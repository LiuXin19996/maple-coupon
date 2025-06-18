package com.fengxin.maplecoupon.engine.service.handler.service;

import com.fengxin.maplecoupon.engine.service.handler.dto.CouponTemplateRemindDTO;

/**
 * 执行提醒用户优惠券模板
 *
 * @author FENGXIN
 * @date 2024/10/30
 */
public interface RemindUserCouponTemplate {
    
    /**
     * 提醒 user coupon
     *
     * @param couponTemplateRemindDTO 优惠券模板提醒DTO
     */
    void executeRemindUserCoupon (CouponTemplateRemindDTO couponTemplateRemindDTO);
}
