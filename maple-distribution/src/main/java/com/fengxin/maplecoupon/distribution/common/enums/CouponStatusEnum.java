package com.fengxin.maplecoupon.distribution.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/24
 * @project feng-coupon
 * @description 优惠券使用状态枚举类
 **/
@Getter
@RequiredArgsConstructor
public enum CouponStatusEnum {
    /**
     * 生效中
     */
    EFFECTIVE(0),
    
    /**
     * 已结束
     */
    ENDED(1);
    
    private final int type;
    
}
