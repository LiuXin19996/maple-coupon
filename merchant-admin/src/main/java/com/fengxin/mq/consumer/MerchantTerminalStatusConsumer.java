package com.fengxin.mq.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fengxin.common.constant.MerchantAdminRedisConstant;
import com.fengxin.common.enums.CouponTemplateStatusEnum;
import com.fengxin.dao.entity.CouponTemplateDO;
import com.fengxin.service.CouponTemplateService;
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
        topic = "merchant-admin-topic",
        consumerGroup = "mapleCoupon_merchant-admin-message-execute-consumer"
)
@Slf4j(topic = "MerchantTerminalStatusConsumer")
public class MerchantTerminalStatusConsumer implements RocketMQListener<JSONObject> {
    private final CouponTemplateService couponTemplateService;
    
    @Override
    public void onMessage (JSONObject message) {
        log.info("[消费者] 优惠券模板定时执行@变更模板表状态 - 执行消费逻辑，消息体：{}", message.toJSONString());
        LambdaUpdateWrapper<CouponTemplateDO> updateWrapper = new LambdaUpdateWrapper<CouponTemplateDO>()
                .eq(CouponTemplateDO::getId, message.getLong ("couponTemplateId"))
                .eq (CouponTemplateDO::getShopNumber, message.getLong ("shopNumber"))
                .set (CouponTemplateDO::getStatus, CouponTemplateStatusEnum.ENDED.getValue ());
        couponTemplateService.update(updateWrapper);
    }
}
