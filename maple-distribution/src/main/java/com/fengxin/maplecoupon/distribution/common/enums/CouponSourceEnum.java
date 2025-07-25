package com.fengxin.maplecoupon.distribution.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/24
 * @project feng-coupon
 * @description 优惠券来源枚举类
 **/
@Getter
@RequiredArgsConstructor
public enum CouponSourceEnum {
    /**
     * 店铺券
     */
    SHOP(0),
    
    /**
     * 平台券
     */
    PLATFORM(1);
    
    private final int type;

}
