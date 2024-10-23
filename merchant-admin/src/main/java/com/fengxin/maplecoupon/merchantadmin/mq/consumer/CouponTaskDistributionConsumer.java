package com.fengxin.maplecoupon.merchantadmin.mq.consumer;

import com.fengxin.maplecoupon.merchantadmin.dto.mq.CouponTaskExecuteEvent;
import com.fengxin.maplecoupon.merchantadmin.dto.mq.MessageWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 优惠券分发消费者
 **/
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "coupon_template_distribution_task_topic",
        consumerGroup = "mapleCoupon_merchant-admin-message-execute-consumer"
)
@Slf4j(topic = "CouponTaskDistributionConsumer")
public class CouponTaskDistributionConsumer implements RocketMQListener<MessageWrapper<CouponTaskExecuteEvent>> {
    
    @Override
    public void onMessage (MessageWrapper<CouponTaskExecuteEvent> message) {
    
    }
}
