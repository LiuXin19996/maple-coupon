package com.fengxin.maplecoupon.engine.common.constant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MQ常量
 *
 * @author fengxin
 * @date 2024-12-22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MQConstant {
    /**
     * Canal binlog同步用户优惠券主题
     */
    public static final String USER_COUPON_CANAL_BINLOG_SYNC_TOPIC = "user-coupon-canal-binlog-sync-topic";
    
    /**
     * Canal binlog同步用户优惠券消费者
     */
    public static final String USER_COUPON_CANAL_BINLOG_SYNC_CONSUMER = "user-coupon-canal-binlog-sync-consumer";
    
    /**
     * 用户优惠券延迟结束主题
     */
    public static final String USER_COUPON_DELAY_CLOSE_TOPIC = "user_coupon-close-execute-topic";
    
    /**
     * 用户优惠券延迟结束消费者
     */
    public static final String USER_COUPON_DELAY_CLOSE_CONSUMER = "user_coupon-close-execute-consumer";
    
    /**
     * 用户优惠券异步兑换主题
     */
    public static final String USER_COUPON_ASYNC_REDEEM_TOPIC = "user_coupon-redemption-async-execute-topic";
    
    /**
     * 用户优惠券异步兑换消费者
     */
    public static final String USER_COUPON_ASYNC_REDEEM_CONSUMER = "user_coupon-redemption-async-execute-consumer";
    
    /**
     * 用户优惠券提醒主题
     */
    public static final String USER_COUPON_REMIND_TOPIC = "user_coupon-remind-execute-topic";
    
    /**
     * 用户优惠券提醒消费者
     */
    public static final String USER_COUPON_REMIND_CONSUMER = "user_coupon-remind-execute-consumer";
}
