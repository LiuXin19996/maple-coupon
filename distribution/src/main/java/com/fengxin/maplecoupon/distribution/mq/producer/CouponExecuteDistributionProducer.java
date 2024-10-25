package com.fengxin.maplecoupon.distribution.mq.producer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.fengxin.maplecoupon.distribution.mq.design.AbstractCommonSendProduceTemplate;
import com.fengxin.maplecoupon.distribution.mq.design.BaseSendExtendDTO;
import com.fengxin.maplecoupon.distribution.mq.design.CouponTemplateDistributionEvent;
import com.fengxin.maplecoupon.distribution.mq.design.MessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author FENGXIN
 * @date 2024/10/25
 * @project feng-coupon
 * @description 优惠券推送任务执行生产者
 **/
@Slf4j
@Component
public class CouponExecuteDistributionProducer extends AbstractCommonSendProduceTemplate<CouponTemplateDistributionEvent> {
    
    public CouponExecuteDistributionProducer (RocketMQTemplate rocketMQTemplate) {
        super (rocketMQTemplate);
    }
    
    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam (CouponTemplateDistributionEvent messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName ("优惠券发放执行")
                .keys (String.valueOf (messageSendEvent.getCouponTaskId ()))
                .topic ("coupon_template_distribution_execute_topic")
                .sentTimeout (2000L)
                .build();
    }
    
    @Override
    protected Message<?> buildMessage (CouponTemplateDistributionEvent messageSendEvent , BaseSendExtendDTO baseSendExtendDTO) {
        String keys = StrUtil.isEmpty (baseSendExtendDTO.getKeys ()) ? UUID.fastUUID ().toString () : baseSendExtendDTO.getKeys ();
        return MessageBuilder
                .withPayload (new MessageWrapper (keys,messageSendEvent))
                .setHeader (MessageConst.PROPERTY_KEYS,keys)
                .setHeader (MessageConst.PROPERTY_TAGS,baseSendExtendDTO.getTag ())
                .build ();
    }
}
