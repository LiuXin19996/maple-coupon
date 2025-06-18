package com.fengxin.maplecoupon.engine.mq.design;

import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateRedeemReqDTO;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author FENGXIN
 * @date 2024/10/29
 * @project feng-coupon
 * @description 用户优惠券兑换事件
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCouponRedeemEvent {
    /**
     * Web 请求参数
     */
    private CouponTemplateRedeemReqDTO requestParam;

    /**
     * 领取次数
     */
    private Integer receiveCount;

    /**
     * 优惠券模板
     */
    private CouponTemplateQueryRespDTO couponTemplate;

    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 时间
     */
    private Date now;
}
