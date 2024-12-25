package com.fengxin.maplecoupon.merchantadmin.common.constant;

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
     * 优惠券模板终止 主题
     */
    public static final String COUPON_TEMPLATE_TERMINAL_TOPIC = "mapleCoupon_merchant_admin-terminal_coupon_template-topic";
    
    /**
     * 优惠券模板终止 消费者组
     *
     */
    public static final String COUPON_TEMPLATE_TERMINAL_CONSUMER_GROUP = "maplecoupon_merchant-admin-terminal-execute-consumer";
    
    /**
     * 优惠券任务分发 刷新行数 延迟队列
     *
     */
    public static final String COUPON_TASK_SEND_NUM_FLUSH_EXCEL_DELAY_QUEUE = "mapleCoupon_distribution_task-send_num-flush_excel-delay_queue";
}
