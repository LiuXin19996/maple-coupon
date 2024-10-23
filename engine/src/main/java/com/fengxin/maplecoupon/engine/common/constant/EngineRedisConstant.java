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
    
    public static final String LOCK_COUPON_TEMPLATE_KEY = "lock:maple-coupon_engine:template:%s";
    
}
