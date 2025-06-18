package com.fengxin.maplecoupon.engine.service;

import com.fengxin.maplecoupon.engine.dto.req.*;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateRemindQueryRespDTO;
import com.fengxin.maplecoupon.engine.service.handler.dto.CouponTemplateRemindDTO;

import java.util.List;

/**
 * @author FENGXIN
 * @date 2024/10/28
 * @project feng-coupon
 * @description 用户兑换优惠券业务层
 **/
public interface UserCouponService{
    /**
     * 兑换用户优惠券v1
     *
     * @param requestParam 请求参数
     */
    void redeemUserCouponv1 (CouponTemplateRedeemReqDTO requestParam);
    
    /**
     * 兑换用户优惠券v2
     *
     * @param requestParam 请求参数
     */
    void redeemUserCouponv2 (CouponTemplateRedeemReqDTO requestParam);
    
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
    
    /**
     * 优惠券提醒列表
     *
     * @param requestParam 请求参数
     * @return {@code List<CouponTemplateRemindQueryRespDTO> }
     */
    List<CouponTemplateRemindQueryRespDTO> listCouponRemind (CouponTemplateRemindQueryReqDTO requestParam);
    
    /**
     * 创建付款记录
     *
     * @param requestParam 请求参数
     */
    void createPaymentRecord (CouponCreatePaymentReqDTO requestParam);
    
    /**
     * 处理付款
     *
     * @param requestParam 请求参数
     */
    void processPayment (CouponProcessPaymentReqDTO requestParam);
    
    /**
     * 办理退款
     *
     * @param requestParam 请求参数
     */
    void processRefund (CouponProcessRefundReqDTO requestParam);
}
