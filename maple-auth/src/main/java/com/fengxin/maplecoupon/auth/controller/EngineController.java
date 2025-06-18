package com.fengxin.maplecoupon.auth.controller;

import com.fengxin.idempotent.DuplicateSubmit;
import com.fengxin.maplecoupon.auth.remote.MapleCouponEngineRemoteService;
import com.fengxin.maplecoupon.auth.remote.dto.req.*;
import com.fengxin.maplecoupon.auth.remote.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.maplecoupon.auth.remote.dto.resp.CouponTemplateRemindQueryRespDTO;
import com.fengxin.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author FENGXIN
 */
@RestController
@Tag(name = "引擎",description = "服务用户")
@RequiredArgsConstructor
public class EngineController {
    private final MapleCouponEngineRemoteService mapleCouponEngineRemoteService;
    @Operation(summary = "查询优惠券模板")
    @GetMapping("/api/auth/engine/coupon-template/query")
    public Result<CouponTemplateQueryRespDTO> findCouponTemplate(CouponTemplateQueryReqDTO requestParam){
        return mapleCouponEngineRemoteService.findCouponTemplate (
                requestParam.getCouponTemplateId (),
                requestParam.getShopNumber ());
    }
    
    @Operation(summary = "兑换优惠券模板", description = "存在较高流量场景，“秒杀”业务")
    @PostMapping("/api/auth/engine/user-coupon/redeem")
    public Result<Void> redeemUserCoupon(@RequestBody CouponTemplateRedeemReqDTO requestParam) {
        return mapleCouponEngineRemoteService.redeemUserCoupon (requestParam);
    }
    
    @DuplicateSubmit(message = "请勿短时间内重复提交相同的修改信息")
    @Operation(summary = "设置优惠券提醒时间")
    @PostMapping("/api/auth/engine/coupon-template-remind/create")
    public Result<Void> createCouponRemind(@RequestBody CouponTemplateRemindTimeReqDTO requestParam) {
        return mapleCouponEngineRemoteService.createCouponRemind (requestParam);
    }
    
    @Operation(summary = "查询优惠券预约提醒")
    @GetMapping("/api/auth/engine/coupon-template-remind/list")
    public Result<List<CouponTemplateRemindQueryRespDTO>> listCouponRemind() {
       return mapleCouponEngineRemoteService.listCouponRemind ();
    }
    
    @Operation(summary = "取消优惠券预约提醒")
    @DuplicateSubmit(message = "请勿短时间内重复提交取消预约提醒请求")
    @PostMapping("/api/auth/engine/coupon-template-remind/cancel")
    public Result<Void> cancelCouponRemind(@RequestBody CouponTemplateRemindCancelReqDTO requestParam) {
        return mapleCouponEngineRemoteService.cancelCouponRemind (requestParam);
    }
    
    @Operation(summary = "创建用户优惠券结算单", description = "用户下单时锁定使用的优惠券，一般由订单系统发起调用")
    @PostMapping("/api/auth/engine/user-coupon/create-payment-record")
    public Result<Void> createPaymentRecord(@RequestBody CouponCreatePaymentReqDTO requestParam) {
        return mapleCouponEngineRemoteService.createPaymentRecord (requestParam);
    }
    
    @Operation(summary = "核销优惠券结算单", description = "用户支付后核销使用的优惠券，常规来说应该监听支付后的消息队列事件")
    @PostMapping("/api/auth/engine/user-coupon/process-payment")
    public Result<Void> processPayment(@RequestBody CouponProcessPaymentReqDTO requestParam) {
        return mapleCouponEngineRemoteService.processPayment (requestParam);
    }
    
    @Operation(summary = "退款优惠券结算单", description = "用户退款成功后返回使用的优惠券，常规来说应该监听退款成功后的消息队列事件")
    @PostMapping("/api/auth/engine/user-coupon/process-refund")
    public Result<Void> processRefund(@RequestBody CouponProcessRefundReqDTO requestParam) {
        return mapleCouponEngineRemoteService.processRefund (requestParam);
    }
}
