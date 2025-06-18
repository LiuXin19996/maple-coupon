package com.fengxin.maplecoupon.merchantadmin.mq.producer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.fengxin.maplecoupon.merchantadmin.mq.design.AbstractCommonSendProduceTemplate;
import com.fengxin.maplecoupon.merchantadmin.mq.design.BaseSendExtendDTO;
import com.fengxin.maplecoupon.merchantadmin.mq.design.CouponTemplateDelayExecuteEvent;
import com.fengxin.maplecoupon.merchantadmin.mq.design.MessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.fengxin.maplecoupon.merchantadmin.common.constant.RocketMQConstant.COUPON_TEMPLATE_TERMINAL_TOPIC;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 优惠券模板结束发送者
 **/
@Slf4j
@Component
public class CouponTemplateDelayTerminalStatusProducer extends AbstractCommonSendProduceTemplate<CouponTemplateDelayExecuteEvent> {

    public CouponTemplateDelayTerminalStatusProducer (RocketMQTemplate rocketMqTemplate) {
        super (rocketMqTemplate);
    }
    
    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam (CouponTemplateDelayExecuteEvent messageSendEvent) {
        return BaseSendExtendDTO.builder ()
                .keys (String.valueOf (messageSendEvent.getCouponTemplateId ()))
                .eventName ("优惠券模板结束")
                .topic (COUPON_TEMPLATE_TERMINAL_TOPIC)
                .delayTime (messageSendEvent.getDelayTime ())
                .build ();
    }
    
    @Override
    protected Message<?> buildMessage (CouponTemplateDelayExecuteEvent messageSendEvent , BaseSendExtendDTO baseSendExtendDTO) {
        String keys = StrUtil.isNotEmpty (baseSendExtendDTO.getKeys ()) ? baseSendExtendDTO.getKeys () : UUID.fastUUID ().toString ();
        return MessageBuilder
                .withPayload (new MessageWrapper<> (keys,messageSendEvent))
                .setHeader (MessageConst.PROPERTY_KEYS,keys)
                .setHeader (MessageConst.PROPERTY_TAGS,baseSendExtendDTO.getTag ())
                .build ();
    }
}
