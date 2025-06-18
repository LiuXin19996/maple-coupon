package com.fengxin.maplecoupon.merchantadmin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券模板枚举
 **/
@RequiredArgsConstructor
public enum CouponTemplateStatusEnum {
    /**
     * 有效
     */
    ACTIVE(0),
    /**
     * 失效
     */
    ENDED(1);
    @Getter
    private final int value;
}
