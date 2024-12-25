package com.fengxin.maplecoupon.engine.mq.producer;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.fengxin.maplecoupon.engine.mq.design.AbstractCommonSendProduceTemplate;
import com.fengxin.maplecoupon.engine.mq.design.BaseSendExtendDTO;
import com.fengxin.maplecoupon.engine.mq.design.MessageWrapper;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponRemindEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static com.fengxin.maplecoupon.engine.common.constant.RocketMQConstant.USER_COUPON_REMIND_TOPIC;

/**
 * @author FENGXIN
 * @date 2024/10/30
 * @project feng-coupon
 * @description 用户优惠券提醒发送者
 **/
@Slf4j
@Component
public class UserCouponRemindProducer extends AbstractCommonSendProduceTemplate<UserCouponRemindEvent> {
    
    public UserCouponRemindProducer (RocketMQTemplate rocketMqTemplate) {
        super (rocketMqTemplate);
    }
    
    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam (UserCouponRemindEvent messageSendEvent) {
        return BaseSendExtendDTO.builder ()
                .eventName ("用户优惠券提醒执行")
                .keys (UUID.fastUUID () + messageSendEvent.getUserId ())
                .topic (USER_COUPON_REMIND_TOPIC)
                .sentTimeout (2000L)
                .delayTime (messageSendEvent.getDelayTime ())
                .build ();
    }
    
    @Override
    protected Message<?> buildMessage (UserCouponRemindEvent messageSendEvent , BaseSendExtendDTO baseSendExtendDTO) {
        String keys = StrUtil.isEmpty (baseSendExtendDTO.getKeys ()) ? UUID.fastUUID () + messageSendEvent.getUserId () : baseSendExtendDTO.getKeys ();
        return MessageBuilder
                .withPayload (new MessageWrapper (keys,messageSendEvent))
                .setHeader (MessageConst.PROPERTY_KEYS,keys)
                .setHeader (MessageConst.PROPERTY_TAGS,baseSendExtendDTO.getTag ())
                .build ();
    }
}
