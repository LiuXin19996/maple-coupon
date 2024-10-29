package com.fengxin.maplecoupon.engine.mq.consumer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fengxin.maplecoupon.engine.common.constant.EngineRedisConstant;
import com.fengxin.maplecoupon.engine.common.context.UserContext;
import com.fengxin.maplecoupon.engine.common.enums.UserCouponStatusEnum;
import com.fengxin.maplecoupon.engine.dao.entity.UserCouponDO;
import com.fengxin.maplecoupon.engine.dao.mapper.UserCouponMapper;
import com.fengxin.maplecoupon.engine.mq.design.MessageWrapper;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponDelayCloseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author FENGXIN
 * @date 2024/10/28
 * @project feng-coupon
 * @description 设置用户优惠券结束延时消息消费者
 **/
@Component
@RequiredArgsConstructor
@RocketMQMessageListener (
        topic = "user-coupon-template_redemption_close_execute_topic",
        consumerGroup = "user-coupon-template_redemption_close_consumer"
)
@Slf4j(topic = "UserCouponDelayCloseConsumer")
public class UserCouponDelayCloseConsumer implements RocketMQListener<MessageWrapper<UserCouponDelayCloseEvent>> {
    private final StringRedisTemplate stringRedisTemplate;
    private final UserCouponMapper userCouponMapper;
    @Override
    public void onMessage (MessageWrapper<UserCouponDelayCloseEvent> message) {
        // 打印日志
        log.info ("[消费者] 用户优惠券延时结束执行 消息体: {}" , JSON.toJSONString (message));
        UserCouponDelayCloseEvent event = message.getMessage ();
        // 删除用户领取优惠券模板缓存记录
        String userCouponListCacheKey = String.format(EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY, UserContext.getUserId());
        String userCouponItemCacheKey = StrUtil.builder()
                .append(event.getCouponTemplateId())
                .append("_")
                .append(event.getUserCouponId())
                .toString();
        Long removed = stringRedisTemplate.opsForZSet().remove(userCouponListCacheKey, userCouponItemCacheKey);
        if (removed == null || removed == 0L) {
            log.error ("删除缓存用户优惠券失败 重试消费");
            return;
        }
        
        // 修改用户领券记录状态为已过期
        UserCouponDO userCouponDO = UserCouponDO.builder()
                .status(UserCouponStatusEnum.EXPIRED.getCode())
                .build();
        LambdaUpdateWrapper<UserCouponDO> updateWrapper = Wrappers.lambdaUpdate(UserCouponDO.class)
                .eq(UserCouponDO::getId, event.getUserCouponId())
                .eq(UserCouponDO::getUserId, event.getUserId())
                .eq(UserCouponDO::getStatus, UserCouponStatusEnum.UNUSED.getCode())
                .eq(UserCouponDO::getCouponTemplateId, event.getCouponTemplateId());
        userCouponMapper.update(userCouponDO, updateWrapper);
        log.info ("[消费者] 用户优惠券延时结束 消费完成");
    }
}
