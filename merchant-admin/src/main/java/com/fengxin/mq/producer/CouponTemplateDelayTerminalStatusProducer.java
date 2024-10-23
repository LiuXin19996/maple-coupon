package com.fengxin.mq.producer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.fengxin.dto.mq.AbstractCommonSendProduceTemplate;
import com.fengxin.dto.mq.BaseSendExtendDTO;
import com.fengxin.dto.mq.CouponTemplateDelayExecuteEvent;
import com.fengxin.dto.mq.MessageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 优惠券模板结束发送者
 **/
@Slf4j
@Component
public class CouponTemplateDelayTerminalStatusProducer extends AbstractCommonSendProduceTemplate<CouponTemplateDelayExecuteEvent> {

    public CouponTemplateDelayTerminalStatusProducer (RocketMQTemplate rocketMQTemplate) {
        super (rocketMQTemplate);
    }
    
    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam (CouponTemplateDelayExecuteEvent messageSendEvent) {
        return BaseSendExtendDTO.builder ()
                .keys (String.valueOf (messageSendEvent.getCouponTemplateId ()))
                .eventName ("优惠券模板结束")
                .topic ("merchant-admin-terminal-coupon-template-topic")
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
