package com.fengxin.maplecoupon.engine.mq.producer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.fengxin.maplecoupon.engine.mq.design.AbstractCommonSendProduceTemplate;
import com.fengxin.maplecoupon.engine.mq.design.BaseSendExtendDTO;
import com.fengxin.maplecoupon.engine.mq.design.MessageWrapper;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponRedeemEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author FENGXIN
 * @date 2024/10/29
 * @project feng-coupon
 * @description 用户优惠券兑换发送者
 **/
@Slf4j
@Component
public class UserCouponRedeemProducer extends AbstractCommonSendProduceTemplate<UserCouponRedeemEvent> {
    
    public UserCouponRedeemProducer (RocketMQTemplate rocketMQTemplate) {
        super (rocketMQTemplate);
    }
    
    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam (UserCouponRedeemEvent messageSendEvent) {
        return BaseSendExtendDTO.builder ()
                .eventName ("用户优惠券兑换落库和Redis")
                .topic ("user-coupon-template_redemption_sync_execute_topic")
                .keys (UUID.fastUUID () + messageSendEvent.getUserId ())
                .sentTimeout (2000L)
                .build ();
    }
    
    @Override
    protected Message<?> buildMessage (UserCouponRedeemEvent messageSendEvent , BaseSendExtendDTO baseSendExtendDTO) {
        String keys = StrUtil.isEmpty (baseSendExtendDTO.getKeys ()) ? UUID.fastUUID ().toString () : baseSendExtendDTO.getKeys ();
        return MessageBuilder
                .withPayload (new MessageWrapper (keys,messageSendEvent))
                .setHeader (MessageConst.PROPERTY_KEYS,keys)
                .setHeader (MessageConst.PROPERTY_TAGS,baseSendExtendDTO.getTag ())
                .build ();
    }
}
