package com.fengxin.maplecoupon.engine.mq.design;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author FENGXIN
 * @date 2024/10/28
 * @project feng-coupon
 * @description 设置用户优惠券结束延时消息
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCouponDelayCloseEvent {
    /**
     * 用户优惠券 id
     */
    private Long userCouponId;
    
    /**
     * 用户id
     */
    private Long userId;
    
    /**
     * 优惠券模板id
     */
    private Long couponTemplateId;
    
    /**
     * 延时时间
     */
    private Long delayDateTime;
}
