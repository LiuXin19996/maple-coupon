package com.fengxin.maplecoupon.settlement.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 优惠券结算单表
 *
 * @author fengxin
 * @date 2024-10-31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_coupon_settlement")
public class CouponSettlementDO {
    /**
    * ID
    */
    private Long id;

    /**
    * 订单ID
    */
    private Long orderId;

    /**
    * 用户ID
    */
    private Long userId;

    /**
    * 用户优惠券ID
    */
    private Long couponId;

    /**
    * 结算单状态 0：锁定 1：已取消 2：已支付 3：已退款
    */
    private Integer status;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 修改时间
    */
    private Date updateTime;
}