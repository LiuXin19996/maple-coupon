package com.fengxin.maplecoupon.settlement.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author FENGXIN
 * @date 2024/10/30
 * @project feng-coupon
 * @description 用户预约提醒信息存储表
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_coupon_template_remind")
public class CouponTemplateRemindDO {
    /**
    * 用户ID
    */
    private Long userId;

    /**
    * 券ID
    */
    private Long couponTemplateId;

    /**
    * 存储信息
    */
    private Long information;

    /**
    * 店铺编号
    */
    private Long shopNumber;

    /**
    * 优惠券开抢时间
    */
    private Date startTime;
}