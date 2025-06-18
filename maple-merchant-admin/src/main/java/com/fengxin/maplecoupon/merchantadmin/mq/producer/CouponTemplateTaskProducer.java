package com.fengxin.maplecoupon.merchantadmin.mq.producer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.fengxin.maplecoupon.merchantadmin.mq.design.AbstractCommonSendProduceTemplate;
import com.fengxin.maplecoupon.merchantadmin.mq.design.BaseSendExtendDTO;
import com.fengxin.maplecoupon.merchantadmin.mq.design.CouponTaskExecuteEvent;
import com.fengxin.maplecoupon.merchantadmin.mq.design.MessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.fengxin.maplecoupon.merchantadmin.common.constant.RocketMQConstant.COUPON_TASK_DISTRIBUTION_TOPIC;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 优惠券模板生产者
 **/
@Slf4j
@Component
public class CouponTemplateTaskProducer extends AbstractCommonSendProduceTemplate<CouponTaskExecuteEvent> {
    
    public CouponTemplateTaskProducer (RocketMQTemplate rocketMQTemplate , ConfigurableEnvironment environment) {
        super (rocketMQTemplate);
    }
    
    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam (CouponTaskExecuteEvent messageSendEvent) {
        return BaseSendExtendDTO.builder ()
                .keys (String.valueOf (messageSendEvent.getCouponTaskId ()))
                .eventName ("优惠券推送任务")
                .topic (COUPON_TASK_DISTRIBUTION_TOPIC)
                .sentTimeout (2000L)
                .build ();
    }
    
    @Override
    protected Message<?> buildMessage (CouponTaskExecuteEvent messageSendEvent , BaseSendExtendDTO baseSendExtendDTO) {
        String keys = StrUtil.isNotEmpty (baseSendExtendDTO.getKeys ()) ? baseSendExtendDTO.getKeys () : UUID.fastUUID ().toString ();
        return MessageBuilder
                .withPayload (new MessageWrapper<> (keys , messageSendEvent))
                .setHeader (MessageConst.PROPERTY_KEYS,keys)
                .setHeader (MessageConst.PROPERTY_TAGS,baseSendExtendDTO.getTag ())
                .build ();
    }
}
