package com.fengxin.maplecoupon.merchantadmin.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fengxin.maplecoupon.merchantadmin.common.enums.CouponTemplateStatusEnum;
import com.fengxin.maplecoupon.merchantadmin.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.merchantadmin.mq.design.CouponTemplateDelayExecuteEvent;
import com.fengxin.maplecoupon.merchantadmin.mq.design.MessageWrapper;
import com.fengxin.maplecoupon.merchantadmin.service.CouponTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author FENGXIN
 * @date 2024/10/21
 * @project feng-coupon
 * @description 优惠券模板结束状态消费者
 **/
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "merchant-admin-terminal-coupon-template-topic",
        consumerGroup = "mapleCoupon_merchant-admin-message-execute-consumer"
)
@Slf4j(topic = "MerchantTerminalStatusConsumer")
public class MerchantTerminalStatusConsumer implements RocketMQListener<MessageWrapper<CouponTemplateDelayExecuteEvent>> {
    private final CouponTemplateService couponTemplateService;
    
    @Override
    public void onMessage (MessageWrapper<CouponTemplateDelayExecuteEvent> message) {
        log.info("[消费者] 优惠券模板定时执行@变更模板表状态 - 执行消费逻辑，消息体：{}", message.getMessage ());
        LambdaUpdateWrapper<CouponTemplateDO> updateWrapper = new LambdaUpdateWrapper<CouponTemplateDO>()
                .eq(CouponTemplateDO::getId, message.getMessage ().getCouponTemplateId ())
                .eq (CouponTemplateDO::getShopNumber, message.getMessage ().getShopNumber ())
                .set (CouponTemplateDO::getStatus, CouponTemplateStatusEnum.ENDED.getValue ());
        couponTemplateService.update(updateWrapper);
    }
}
