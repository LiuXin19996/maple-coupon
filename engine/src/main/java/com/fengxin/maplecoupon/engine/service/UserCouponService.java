package com.fengxin.maplecoupon.engine.service;

import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateRedeemReqDTO;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateRemindCancelReqDTO;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateRemindTimeReqDTO;
import com.fengxin.maplecoupon.engine.service.handler.dto.CouponTemplateRemindDTO;

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
    
    /**
     * 创建优惠券提醒
     *
     * @param requestParam 请求参数
     */
    void createCouponRemind (CouponTemplateRemindTimeReqDTO requestParam);
    
    /**
     * 取消优惠券提醒
     *
     * @param requestParam 请求参数
     */
    void cancelCouponRemind (CouponTemplateRemindCancelReqDTO requestParam);
    
    /**
     * 判断是否取消预约
     *
     * @param requestParam 优惠券模板提醒DTO
     * @return boolean
     */
    boolean isCanalRemind(CouponTemplateRemindDTO requestParam);
}
