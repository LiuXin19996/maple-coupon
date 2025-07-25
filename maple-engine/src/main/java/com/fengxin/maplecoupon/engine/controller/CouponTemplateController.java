package com.fengxin.maplecoupon.engine.controller;

import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateQueryReqDTO;

import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.maplecoupon.engine.service.CouponTemplateService;
import com.fengxin.web.Result;
import com.fengxin.web.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description
 **/
@RestController
@RequiredArgsConstructor
@Tag (name = "优惠券模板管理")
public class CouponTemplateController {
    private final CouponTemplateService couponTemplateService;
    
    @Operation(summary = "查询优惠券模板")
    @GetMapping("/api/engine/coupon-template/query")
    public Result<CouponTemplateQueryRespDTO> findCouponTemplate(CouponTemplateQueryReqDTO requestParam) {
        return Results.success(couponTemplateService.findCouponTemplateById(requestParam));
    }
    
}
