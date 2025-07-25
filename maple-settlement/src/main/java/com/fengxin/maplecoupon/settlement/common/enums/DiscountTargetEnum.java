package com.fengxin.maplecoupon.settlement.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券优惠对象枚举
 **/
@Getter
@RequiredArgsConstructor
public enum DiscountTargetEnum {

    /**
     * 商品专属优惠
     */
    PRODUCT_SPECIFIC(0, "商品专属优惠"),
    /**
     * 全店通用优惠
     */
    ALL_STORE_GENERAL(1, "全店通用优惠");
    
    private final int type;
    
    private final String value;
    
    /**
     * 根据 type 找到对应的 value
     *
     * @param type 要查找的类型代码
     * @return 对应的描述值，如果没有找到抛异常
     */
    public static String findValueByType(int type) {
        for (DiscountTargetEnum target : values()) {
            if (target.getType() == type) {
                return target.getValue();
            }
        }
        throw new IllegalArgumentException();
    }
    
}
