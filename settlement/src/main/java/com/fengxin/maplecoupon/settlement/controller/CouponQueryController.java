package com.fengxin.maplecoupon.settlement.controller;

import com.fengxin.maplecoupon.settlement.dto.req.QueryCouponsReqDTO;
import com.fengxin.maplecoupon.settlement.dto.resp.QueryCouponsRespDTO;
import com.fengxin.maplecoupon.settlement.service.CouponQueryService;
import com.fengxin.web.Result;
import com.fengxin.web.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券列表控制器
 *
 * @author fengxin
 * @date 2024-11-02
 */
@RestController
@RequiredArgsConstructor
@Tag (name = "查询用户优惠券管理")
public class CouponQueryController {

    private final CouponQueryService couponQueryService;

    @Operation(summary = "查询用户可用/不可用优惠券列表")
    @PostMapping("/api/settlement/coupon-query")
    public Result<QueryCouponsRespDTO> listQueryCoupons(@RequestBody QueryCouponsReqDTO requestParam) {
        return Results.success(couponQueryService.listQueryUserCoupons(requestParam));
    }

    @Operation(summary = "同步查询用户可用/不可用优惠券列表")
    @PostMapping("/api/settlement/coupon-query-sync")
    public Result<QueryCouponsRespDTO> listQueryCouponsBySync(@RequestBody QueryCouponsReqDTO requestParam) {
        return Results.success(couponQueryService.listQueryUserCouponsBySync(requestParam));
    }
}
