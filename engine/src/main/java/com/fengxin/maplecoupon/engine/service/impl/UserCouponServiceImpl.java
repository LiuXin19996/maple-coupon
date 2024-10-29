package com.fengxin.maplecoupon.engine.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.fengxin.exception.ServiceException;
import com.fengxin.maplecoupon.engine.common.constant.EngineRedisConstant;
import com.fengxin.maplecoupon.engine.common.context.UserContext;
import com.fengxin.maplecoupon.engine.common.enums.RedisStockDecrementErrorEnum;
import com.fengxin.maplecoupon.engine.common.enums.UserCouponStatusEnum;
import com.fengxin.maplecoupon.engine.dao.entity.UserCouponDO;
import com.fengxin.maplecoupon.engine.dao.mapper.CouponTemplateMapper;
import com.fengxin.maplecoupon.engine.dao.mapper.UserCouponMapper;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateQueryReqDTO;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateRedeemReqDTO;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponDelayCloseEvent;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponRedeemEvent;
import com.fengxin.maplecoupon.engine.mq.producer.UserCouponDelayCloseProducer;
import com.fengxin.maplecoupon.engine.mq.producer.UserCouponRedeemProducer;
import com.fengxin.maplecoupon.engine.service.CouponTemplateService;
import com.fengxin.maplecoupon.engine.service.UserCouponService;
import com.fengxin.maplecoupon.engine.util.StockDecrementReturnCombinedUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;

import static com.fengxin.maplecoupon.engine.common.constant.EngineRedisConstant.COUPON_TEMPLATE_KEY;
import static com.fengxin.maplecoupon.engine.common.constant.EngineRedisConstant.USER_COUPON_TEMPLATE_LIMIT_KEY;

/**
 * @author FENGXIN
 * @date 2024/10/28
 * @project feng-coupon
 * @description 用户兑换优惠券业务实现
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {
    private final CouponTemplateService couponTemplateService;
    private final StringRedisTemplate stringRedisTemplate;
    private final TransactionTemplate transactionTemplate;
    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;
    private final UserCouponDelayCloseProducer userCouponDelayCloseProducer;
    private final UserCouponRedeemProducer userCouponRedeemProducer;
    private static final String STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_PATH = "lua/stock_decrement_and_save_user_receive.lua";
    @Value ("one-coupon.user-coupon-list.save-cache.type")
    private String userCouponListSaveCacheType;
    @Override
    public void redeemUserCoupon (CouponTemplateRedeemReqDTO requestParam) {
        // 校验优惠券是否存在缓存 存在数据 且在有效期
        CouponTemplateQueryRespDTO couponTemplateById = couponTemplateService.findCouponTemplateById (BeanUtil.toBean (requestParam , CouponTemplateQueryReqDTO.class));
        if (ObjectUtil.isNull (couponTemplateById)){
            throw new ServiceException ("兑换目标优惠券不存在" + requestParam.toString ());
        }
        Date now = new Date ();
        boolean isIn = DateUtil.isIn (now , couponTemplateById.getValidStartTime () , couponTemplateById.getValidEndTime ());
        if (BooleanUtil.isFalse (isIn)){
            // 可认为是客户端恶意攻击
            throw new ServiceException ("兑换目标优惠券不在有效期范围");
        }
        // 校验通过 扣减缓存的优惠券库存 并验证用户是否超额领取优惠券
        // 将lua脚本放入Hutool容器
        DefaultRedisScript<Long> defaultRedisScript = Singleton.get (STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_PATH , () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<> ();
            redisScript.setScriptSource (new ResourceScriptSource (new ClassPathResource (STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_PATH)));
            redisScript.setResultType (Long.class);
            return redisScript;
        });
        // 领取上限
        JSONObject receiveRule = JSON.parseObject (couponTemplateById.getReceiveRule ());
        String limitPerPerson = receiveRule.getString ("limitPerPerson");
        String couponTemplateKey = String.format (COUPON_TEMPLATE_KEY , requestParam.getCouponTemplateId ());
        String userCouponTemplateKey = String.format (USER_COUPON_TEMPLATE_LIMIT_KEY , UserContext.getUserId () , requestParam.getCouponTemplateId ());
        Long executeResult = stringRedisTemplate.execute (
                defaultRedisScript ,
                List.of (couponTemplateKey , userCouponTemplateKey) ,
                String.valueOf (couponTemplateById.getValidEndTime ().getTime ()) ,
                limitPerPerson
        );
        // 处理执行返回失败结果
        // 0 代表请求成功 1 代表优惠券已被领取完 2 代表用户已经达到领取上限
        // 用户领取次数 初始化为 0，每次领取成功后自增加 1
        long firstField = StockDecrementReturnCombinedUtil.extractFirstField (executeResult);
        if (RedisStockDecrementErrorEnum.isFail (firstField)){
            throw new ServiceException (RedisStockDecrementErrorEnum.fromType (firstField));
        }
        UserCouponRedeemEvent userCouponRedeemEvent = UserCouponRedeemEvent.builder ()
                .requestParam (requestParam)
                .couponTemplate (couponTemplateById)
                .receiveCount ((int) StockDecrementReturnCombinedUtil.extractSecondField (executeResult))
                .userId (UserContext.getUserId ())
                .now (now)
                .build ();
        userCouponRedeemProducer.sendMessage (userCouponRedeemEvent);
    }
}
