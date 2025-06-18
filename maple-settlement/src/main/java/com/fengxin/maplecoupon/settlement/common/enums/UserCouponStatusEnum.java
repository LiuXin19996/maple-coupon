package com.fengxin.maplecoupon.settlement.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/28
 * @project feng-coupon
 * @description 用户优惠券状态枚举
 **/
@Getter
@RequiredArgsConstructor
public enum UserCouponStatusEnum {
    /**
     * 未使用
     */
    UNUSED(0),
    
    /**
     * 锁定
     */
    LOCKING(1),
    
    /**
     * 已使用
     */
    USED(2),
    
    /**
     * 已过期
     */
    EXPIRED(3),
    
    /**
     * 已撤回
     */
    REVOKED(4);
    
    private final int code;

}
