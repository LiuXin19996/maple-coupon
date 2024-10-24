package com.fengxin.maplecoupon.engine.common.constant;

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
     * 优惠券分布式🔒
     */
    public static final String LOCK_COUPON_TEMPLATE_KEY = "lock:maple-coupon_engine:template:%s";
    
    /**
     * 优惠券缓存空值
     */
    public static final String EMPTY_COUPON_TEMPLATE_KEY = "empty:maple-coupon_engine:%s";
    
}
