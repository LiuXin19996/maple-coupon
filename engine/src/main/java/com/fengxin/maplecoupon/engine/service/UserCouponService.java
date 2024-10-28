package com.fengxin.maplecoupon.engine.service;

import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateRedeemReqDTO;

/**
 * @author FENGXIN
 * @date 2024/10/28
 * @project feng-coupon
 * @description 用户兑换优惠券业务层
 **/
public interface UserCouponService{
    /**
     * 兑换用户优惠券
     *
     * @param requestParam 请求参数
     */
    void redeemUserCoupon (CouponTemplateRedeemReqDTO requestParam);
}
