package com.fengxin.maplecoupon.engine.mq.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.fengxin.maplecoupon.engine.mq.design.MessageWrapper;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponRemindEvent;
import com.fengxin.maplecoupon.engine.service.handler.dto.CouponTemplateRemindDTO;
import com.fengxin.maplecoupon.engine.service.handler.service.RemindUserCouponTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.fengxin.maplecoupon.engine.common.constant.RocketMQConstant.USER_COUPON_REMIND_CONSUMER;
import static com.fengxin.maplecoupon.engine.common.constant.RocketMQConstant.USER_COUPON_REMIND_TOPIC;

/**
 * @author FENGXIN
 * @date 2024/10/30
 * @project feng-coupon
 * @description 提醒用户优惠券消费者
 **/
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener (
        topic = USER_COUPON_REMIND_TOPIC,
        consumerGroup = USER_COUPON_REMIND_CONSUMER
)
public class UserCouponRemindDelayConsumer implements RocketMQListener<MessageWrapper<UserCouponRemindEvent>> {
    private final RemindUserCouponTemplate remindUserCouponTemplate;
    @Override
    public void onMessage (MessageWrapper<UserCouponRemindEvent> message) {
        log.info ("[消费者] 提醒用户优惠券执行 消息体{}" , JSON.toJSONString (message));
        UserCouponRemindEvent userCouponRemindEvent = message.getMessage ();
        CouponTemplateRemindDTO couponTemplateRemindDTO = BeanUtil.toBean (userCouponRemindEvent , CouponTemplateRemindDTO.class);
        remindUserCouponTemplate.executeRemindUserCoupon(couponTemplateRemindDTO);
    }
}
