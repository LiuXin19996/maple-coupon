package com.fengxin.maplecoupon.distribution.mq.consumer;

import com.fengxin.maplecoupon.distribution.mq.design.CouponTemplateDistributionEvent;
import com.fengxin.maplecoupon.distribution.mq.design.MessageWrapper;
import org.apache.rocketmq.spring.core.RocketMQListener;

/**
 * @author FENGXIN
 * @date 2024/10/25
 * @project feng-coupon
 * @description 优惠券分发执行
 **/
public class CouponExecuteDistributionConsumer implements RocketMQListener<MessageWrapper<CouponTemplateDistributionEvent>> {
    
    @Override
    public void onMessage (MessageWrapper<CouponTemplateDistributionEvent> message) {
    
    }
}
