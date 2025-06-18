package com.fengxin.maplecoupon.distribution.mq.design;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/25
 * @project feng-coupon
 * @description 优惠券推送任务事件
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponTemplateDistributionEvent {
    /**
     * 优惠券分发任务id
     */
    private Long couponTaskId;
    
    /**
     * 优惠券分发任务批量id
     */
    private Long couponTaskBatchId;
    
    /**
     * 通知方式，可组合使用 0：站内信 1：弹框推送 2：邮箱 3：短信
     */
    private String notifyType;
    
    /**
     * 店铺编号
     */
    private Long shopNumber;
    
    /**
     * 优惠券模板id
     */
    private Long couponTemplateId;
    
    /**
     * 消耗规则
     */
    private String couponTemplateConsumeRule;
    
    /**
     * 用户id
     */
    private String userId;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String mail;
    
    /**
     * 批量保存用户优惠券 Set 长度，默认满 5000 才会批量保存数据库
     */
    private Integer batchUserSetSize;
    
    /**
     * 分发结束标识
     */
    private Boolean distributionEndFlag;
    
}
