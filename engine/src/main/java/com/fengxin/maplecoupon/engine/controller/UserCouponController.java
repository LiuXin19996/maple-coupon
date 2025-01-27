package com.fengxin.maplecoupon.engine.controller;

import com.fengxin.idempotent.DuplicateSubmit;
import com.fengxin.maplecoupon.engine.common.context.UserContext;
import com.fengxin.maplecoupon.engine.dto.req.*;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateRemindQueryRespDTO;
import com.fengxin.maplecoupon.engine.service.UserCouponService;
import com.fengxin.web.Result;
import com.fengxin.web.Results;
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
 * @date 2024/10/28
 * @project feng-coupon
 * @description
 **/
@RestController
@RequiredArgsConstructor
@Tag(name = "用户兑换优惠券管理")
public class UserCouponController {
    private final UserCouponService userCouponService;
    
    @Operation(summary = "兑换优惠券模板", description = "存在较高流量场景，可类比“秒杀”业务")
    @PostMapping("/api/engine/user-coupon/redeem")
    public Result<Void> redeemUserCoupon(@RequestBody CouponTemplateRedeemReqDTO requestParam) {
        userCouponService.redeemUserCouponv1(requestParam);
        return Results.success();
    }
    
    @DuplicateSubmit(message = "请勿短时间内重复提交相同的修改信息")
    @Operation(summary = "设置优惠券提醒时间")
    @PostMapping("/api/engine/coupon-template-remind/create")
    public Result<Void> createCouponRemind(@RequestBody CouponTemplateRemindTimeReqDTO requestParam) {
        userCouponService.createCouponRemind(requestParam);
        return Results.success();
    }
    
    @Operation(summary = "查询优惠券预约提醒")
    @GetMapping("/api/engine/coupon-template-remind/list")
    public Result<List<CouponTemplateRemindQueryRespDTO>> listCouponRemind() {
        return Results.success(userCouponService.listCouponRemind(new CouponTemplateRemindQueryReqDTO (UserContext.getUserId())));
    }
    
    @Operation(summary = "取消优惠券预约提醒")
    @DuplicateSubmit(message = "请勿短时间内重复提交取消预约提醒请求")
    @PostMapping("/api/engine/coupon-template-remind/cancel")
    public Result<Void> cancelCouponRemind(@RequestBody CouponTemplateRemindCancelReqDTO requestParam) {
        userCouponService.cancelCouponRemind(requestParam);
        return Results.success();
    }
    
    @Operation(summary = "创建用户优惠券结算单", description = "用户下单时锁定使用的优惠券，一般由订单系统发起调用")
    @PostMapping("/api/engine/user-coupon/create-payment-record")
    public Result<Void> createPaymentRecord(@RequestBody CouponCreatePaymentReqDTO requestParam) {
        userCouponService.createPaymentRecord(requestParam);
        return Results.success();
    }
    
    @Operation(summary = "核销优惠券结算单", description = "用户支付后核销使用的优惠券，常规来说应该监听支付后的消息队列事件")
    @PostMapping("/api/engine/user-coupon/process-payment")
    public Result<Void> processPayment(@RequestBody CouponProcessPaymentReqDTO requestParam) {
        userCouponService.processPayment(requestParam);
        return Results.success();
    }
    
    @Operation(summary = "退款优惠券结算单", description = "用户退款成功后返回使用的优惠券，常规来说应该监听退款成功后的消息队列事件")
    @PostMapping("/api/engine/user-coupon/process-refund")
    public Result<Void> processRefund(@RequestBody CouponProcessRefundReqDTO requestParam) {
        userCouponService.processRefund(requestParam);
        return Results.success();
    }
    
}
