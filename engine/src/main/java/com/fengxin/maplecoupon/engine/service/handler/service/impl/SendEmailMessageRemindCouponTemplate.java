package com.fengxin.maplecoupon.engine.service.handler.service.impl;

import com.fengxin.maplecoupon.engine.service.handler.dto.CouponTemplateRemindDTO;
import com.fengxin.maplecoupon.engine.service.handler.service.RemindCouponTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 发送电子邮件提醒优惠券模板
 *
 * @author fengxin
 * @date 2024-10-30
 */
@Slf4j
@Service
public class SendEmailMessageRemindCouponTemplate implements RemindCouponTemplate {
    
    @Override
    public void remind (CouponTemplateRemindDTO couponTemplateRemindDTO) {
    
    }
}
