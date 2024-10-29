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
import com.fengxin.maplecoupon.engine.mq.producer.UserCouponDelayCloseProducer;
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
        long secondField = StockDecrementReturnCombinedUtil.extractSecondField (executeResult);
        // 使用编程式事务 处理优惠券扣减和新增用户优惠券
        transactionTemplate.executeWithoutResult (status ->{
            try {
                int decremented = couponTemplateMapper.decrementCouponTemplateStock (
                        Long.parseLong (requestParam.getShopNumber ())
                        , Long.parseLong (requestParam.getCouponTemplateId ())
                        , 1);
                if (!SqlHelper.retBool (decremented)){
                    throw new ServiceException ("您慢了一点点哦 优惠券被抢光啦");
                }
                // 新增用户优惠券
                DateTime validityPeriod = DateUtil.offsetHour (now , JSON.parseObject (couponTemplateById.getConsumeRule ()).getInteger ("validityPeriod"));
                UserCouponDO userCouponDO = UserCouponDO.builder ()
                        .couponTemplateId (Long.parseLong (requestParam.getCouponTemplateId ()))
                        .userId (Long.parseLong (UserContext.getUserId ()))
                        .source (requestParam.getSource ())
                        .receiveCount ((int) secondField)
                        .receiveTime (now)
                        .status (UserCouponStatusEnum.UNUSED.getCode ())
                        .validStartTime (now)
                        .validStartTime (couponTemplateById.getValidEndTime ())
                        .build ();
                userCouponMapper.insert (userCouponDO);
                // 接触强依赖canal配置 在该流程下操作
                if (StrUtil.equals (userCouponListSaveCacheType,"direct")){
                    // 添加用户领取优惠券模板缓存记录
                    String userCouponListCacheKey = String.format(EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY, UserContext.getUserId());
                    String userCouponItemCacheKey = StrUtil.builder()
                            .append(requestParam.getCouponTemplateId())
                            .append("_")
                            .append(userCouponDO.getId())
                            .toString();
                    stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
                    // 防止Redis宕机 造成数据丢失
                    Double score;
                    try {
                        score = stringRedisTemplate.opsForZSet ().score (userCouponListCacheKey,userCouponItemCacheKey);
                        // 插入失败 再插入一次
                        if (ObjectUtil.isNull (score)){
                            // 如果这里还是失败 做不到万无一失 只能增加成功的概率
                            stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
                        }
                    }catch (Throwable e){
                        log.warn("查询Redis用户优惠券记录为空或抛异常，可能Redis宕机或主从复制数据丢失，基础错误信息：{}", e.getMessage());
                        // 如果直接抛异常大概率 Redis 宕机了，所以应该写个延时队列向 Redis 重试放入值。为了避免代码复杂性，这里直接写新增，大家知道最优解决方案即可
                        stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
                    }
                    // 发送延时消息 结束用户优惠券
                    UserCouponDelayCloseEvent userCouponDelayCloseEvent = UserCouponDelayCloseEvent.builder ()
                            .userCouponId (userCouponDO.getId ())
                            .couponTemplateId (Long.valueOf (requestParam.getCouponTemplateId ()))
                            .userId (userCouponDO.getUserId ())
                            .delayDateTime (validityPeriod.getTime ())
                            .build ();
                    SendResult sendResult = userCouponDelayCloseProducer.sendMessage (userCouponDelayCloseEvent);
                    // 发送消息失败解决方案简单且高效的逻辑之一：打印日志并报警，通过日志搜集并重新投递
                    if (ObjectUtil.notEqual(sendResult.getSendStatus().name(), "SEND_OK")) {
                        log.warn("发送优惠券关闭延时队列失败，消息参数：{}", JSON.toJSONString(userCouponDelayCloseEvent));
                    }
                }
            } catch (Throwable ex) {
                status.setRollbackOnly();
                // 优惠券已被领取完业务异常
                if (ex instanceof ServiceException) {
                    throw (ServiceException) ex;
                }
                if (ex instanceof DuplicateKeyException) {
                    log.error("用户重复领取优惠券，用户ID：{}，优惠券模板ID：{}", UserContext.getUserId(), requestParam.getCouponTemplateId());
                    throw new ServiceException("用户重复领取优惠券");
                }
                throw new ServiceException("优惠券领取异常，请稍候再试");
            }
        });
    }
}
