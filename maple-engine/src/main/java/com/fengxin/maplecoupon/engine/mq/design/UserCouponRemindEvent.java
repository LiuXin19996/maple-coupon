package com.fengxin.maplecoupon.engine.mq.design;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author FENGXIN
 * @date 2024/10/30
 * @project feng-coupon
 * @description 用户优惠券提醒事件
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCouponRemindEvent {
    /**
     * 优惠券模板id
     */
    private String couponTemplateId;
    
    /**
     * 店铺编号
     */
    private String shopNumber;
    
    /**
     * 用户id
     */
    private String userId;
    
    /**
     * 用户联系方式，可能是邮箱、手机号、等等
     */
    private String contact;
    
    /**
     * 提醒方式
     */
    private Integer type;
    
    /**
     * 提醒时间，比如五分钟，十分钟，十五分钟
     */
    private Integer remindTime;
    
    /**
     * 开抢时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    
    /**
     * 具体延迟时间
     */
    private Long delayTime;
    
}
