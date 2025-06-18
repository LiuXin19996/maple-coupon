package com.fengxin.maplecoupon.merchantadmin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 优惠券模板删除状态枚举
 *
 * @author fengxin
 * @date 2025-01-27
 */
@Getter
@RequiredArgsConstructor
public enum CouponTemplateDelFlagEnum {
    /**
     * 删除
     */
    DELETED(1, "删除"),
    /**
     * 未删除
     */
    UN_DELETED(0,"未删除");
    private final int value;
    private final String desc;
}
