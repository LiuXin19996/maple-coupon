package com.fengxin.maplecoupon.engine.mq.producer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.fengxin.maplecoupon.engine.mq.design.AbstractCommonSendProduceTemplate;
import com.fengxin.maplecoupon.engine.mq.design.BaseSendExtendDTO;
import com.fengxin.maplecoupon.engine.mq.design.MessageWrapper;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponDelayCloseEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author FENGXIN
 * @date 2024/10/28
 * @project feng-coupon
 * @description 设置用户优惠券结束延时消息发送者
 **/
@Slf4j
@Component
public class UserCouponDelayCloseProducer extends AbstractCommonSendProduceTemplate<UserCouponDelayCloseEvent> {
    
    public UserCouponDelayCloseProducer (RocketMQTemplate rocketMQTemplate) {
        super (rocketMQTemplate);
    }
    
    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam (UserCouponDelayCloseEvent messageSendEvent) {
        return BaseSendExtendDTO.builder ()
                .eventName ("用户优惠券延时结束执行")
                .topic ("user_coupon_template_redemption_close_execute_topic")
                .keys (String.valueOf (messageSendEvent.getUserCouponId ()))
                .sentTimeout (2000L)
                .delayTime (messageSendEvent.getDelayDateTime ())
                .build ();
    }
    
    @Override
    protected Message<?> buildMessage (UserCouponDelayCloseEvent messageSendEvent , BaseSendExtendDTO baseSendExtendDTO) {
        String keys = StrUtil.isEmpty (baseSendExtendDTO.getKeys ()) ? UUID.fastUUID ().toString () : baseSendExtendDTO.getKeys ();
        return MessageBuilder
                .withPayload (new MessageWrapper (keys,messageSendEvent))
                .setHeader (MessageConst.PROPERTY_KEYS,keys)
                .setHeader (MessageConst.PROPERTY_TAGS,baseSendExtendDTO.getTag ())
                .build ();
    }
}
