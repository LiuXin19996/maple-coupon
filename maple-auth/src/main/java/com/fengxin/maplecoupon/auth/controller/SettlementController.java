package com.fengxin.maplecoupon.auth.controller;

import com.fengxin.maplecoupon.auth.remote.MapleCouponSettlementRemoteService;
import com.fengxin.maplecoupon.auth.remote.dto.req.QueryCouponsReqDTO;
import com.fengxin.maplecoupon.auth.remote.dto.resp.QueryCouponsRespDTO;
import com.fengxin.web.Result;
import com.fengxin.web.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author FENGXIN
 */
@RestController
@Tag(name = "结算服务")
@RequiredArgsConstructor
public class SettlementController {
    private final MapleCouponSettlementRemoteService mapleCouponSettlementRemoteService;
    
    @Operation(summary = "异步查询用户可用/不可用优惠券列表")
    @PostMapping("/api/auth/settlement/coupon-query")
    public Result<QueryCouponsRespDTO> listQueryCoupons(@RequestBody QueryCouponsReqDTO requestParam) {
        return mapleCouponSettlementRemoteService.listQueryCoupons (requestParam);
    }
    
    @Operation(summary = "同步查询用户可用/不可用优惠券列表")
    @PostMapping("/api/auth/settlement/coupon-query-sync")
    public Result<QueryCouponsRespDTO> listQueryCouponsBySync(@RequestBody QueryCouponsReqDTO requestParam) {
        return mapleCouponSettlementRemoteService.listQueryCouponsBySync (requestParam);
    }
}
