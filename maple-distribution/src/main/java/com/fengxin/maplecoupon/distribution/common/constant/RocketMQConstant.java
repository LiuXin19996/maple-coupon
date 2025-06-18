package com.fengxin.maplecoupon.distribution.common.constant;

/**
 * MQ常量
 *
 * @author fengxin
 * @date 2024-12-22
 */
public class RocketMQConstant {
    
    /**
     * 优惠券任务分发 任务 主题
     */
    public static final String COUPON_TASK_DISTRIBUTION_TOPIC = "mapleCoupon_distribution_task_topic";
    
    /**
     * 优惠券任务分发 任务 消费者组
     */
    public static final String COUPON_TASK_DISTRIBUTION_CONSUMER_GROUP = "mapleCoupon_distribution-task-consumer";
    
    /**
     * 优惠券模板分发 执行 主题
     */
    public static final String COUPON_EXECUTE_DISTRIBUTION_TOPIC = "mapleCoupon_distribution_execute_topic";
    
    /**
     * 优惠券模板分发 执行 消费者组
     */
    public static final String COUPON_EXECUTE_DISTRIBUTION_CONSUMER_GROUP = "mapleCoupon_distribution-execute-consumer";
}
