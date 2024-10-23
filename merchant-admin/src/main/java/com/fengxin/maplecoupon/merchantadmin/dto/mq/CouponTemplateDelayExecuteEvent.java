package com.fengxin.maplecoupon.merchantadmin.dto.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 优惠券模板结束状态事件
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponTemplateDelayExecuteEvent {
    /**
     * 店铺id
     */
    private Long shopNumber;
    
    /**
     * 优惠券模板id
     */
    private Long couponTemplateId;
    
    /**
     * 具体延迟时间
     */
    private Long delayTime;
    
}
