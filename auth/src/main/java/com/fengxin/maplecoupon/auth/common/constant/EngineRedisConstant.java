package com.fengxin.maplecoupon.auth.common.constant;

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
     * 优惠券分布式🔒 Key
     */
    public static final String LOCK_COUPON_TEMPLATE_KEY = "lock:maple-coupon_engine:template:%s";
    
    /**
     * 优惠券缓存空值 Key
     */
    public static final String EMPTY_COUPON_TEMPLATE_KEY = "empty:maple-coupon_engine:%s";
    
    /**
     * 限制用户领取优惠券模板次数缓存 Key
     */
    public static final String USER_COUPON_TEMPLATE_LIMIT_KEY = "maple-coupon_engine:user-template-limit:%s_%s";
    
    /**
     * 用户已领取优惠券列表模板 Key
     */
    public static final String USER_COUPON_TEMPLATE_LIST_KEY = "maple-coupon_engine:user-template-list:%s";
    
    /**
     * 检查用户是否已提醒 Key
     */
    public static final String COUPON_REMIND_CHECK_KEY = "maple-coupon_engine:coupon-remind-check:%s_%s_%d_%d";
    
    /**
     * 用户优惠券提醒 Key
     */
    public static final String USER_COUPON_REMIND_KEY = "maple-coupon_engine:coupon-remind:%s";
    
    /**
     * 锁定优惠券结算分布式🔒 Key
     */
    public static final String LOCK_COUPON_SETTLEMENT_KEY = "lock:maple-coupon_engine:settlement:%s";
    
    public static final String LOCK_COUPON_USER_REGISTER_KEY = "lock:maple-coupon_auth:user:register_username:";
    
    public static final String COUPON_USER_LOGIN_KEY = "maple-coupon_auth:user:login:";
}
