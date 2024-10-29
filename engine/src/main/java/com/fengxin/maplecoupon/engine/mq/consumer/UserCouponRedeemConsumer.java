package com.fengxin.maplecoupon.engine.mq.consumer;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.fengxin.exception.ServiceException;
import com.fengxin.maplecoupon.engine.common.constant.EngineRedisConstant;
import com.fengxin.maplecoupon.engine.common.context.UserContext;
import com.fengxin.maplecoupon.engine.common.enums.UserCouponStatusEnum;
import com.fengxin.maplecoupon.engine.dao.entity.UserCouponDO;
import com.fengxin.maplecoupon.engine.dao.mapper.CouponTemplateMapper;
import com.fengxin.maplecoupon.engine.dao.mapper.UserCouponMapper;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateRedeemReqDTO;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.maplecoupon.engine.mq.design.MessageWrapper;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponDelayCloseEvent;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponRedeemEvent;
import com.fengxin.maplecoupon.engine.mq.producer.UserCouponDelayCloseProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author FENGXIN
 * @date 2024/10/29
 * @project feng-coupon
 * @description
 **/
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener (
        topic = "user-coupon-template_redemption_sync_execute_topic",
        consumerGroup = "user-coupon-template_redemption_consumer"
)
public class UserCouponRedeemConsumer implements RocketMQListener<MessageWrapper<UserCouponRedeemEvent>> {
    private final StringRedisTemplate stringRedisTemplate;
    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;
    private final UserCouponDelayCloseProducer userCouponDelayCloseProducer;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage (MessageWrapper<UserCouponRedeemEvent> message) {
        // 打印日志
        log.info ("[消费者] 用户优惠券延时结束执行 消息体: {}" , JSON.toJSONString (message));
        UserCouponRedeemEvent userCouponRedeemEvent = message.getMessage ();
        CouponTemplateQueryRespDTO couponTemplateById = userCouponRedeemEvent.getCouponTemplate ();
        CouponTemplateRedeemReqDTO requestParam = userCouponRedeemEvent.getRequestParam ();
        Integer receiveCount = userCouponRedeemEvent.getReceiveCount ();
        Date now = userCouponRedeemEvent.getNow ();
        String userId = userCouponRedeemEvent.getUserId ();
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
                .userId (Long.valueOf (userId))
                .source (requestParam.getSource ())
                .receiveCount ( receiveCount)
                .receiveTime (now)
                .status (UserCouponStatusEnum.UNUSED.getCode ())
                .validStartTime (now)
                .validStartTime (couponTemplateById.getValidEndTime ())
                .build ();
        userCouponMapper.insert (userCouponDO);
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
            // 如果直接抛异常大概率 Redis 宕机了，所以应该写个延时队列向 Redis 重试放入值。为了避免代码复杂性，这里直接写新增，知道最优解决方案即可
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
}
