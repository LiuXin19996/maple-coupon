package com.fengxin.maplecoupon.distribution.common.constant;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description redis常量
 **/
public final class EngineRedisConstant {
    /**
     * 优惠券模板缓存 Key
     */
    public static final String COUPON_TEMPLATE_KEY = "maple-coupon_engine:template:%s";
    /**
     * 用户领取优惠券缓存key
     */
    public static final String USER_COUPON_TEMPLATE_LIST_KEY = "maple-coupon-user-template-list:%s";
    
}
