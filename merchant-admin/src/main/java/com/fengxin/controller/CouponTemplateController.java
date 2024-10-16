package com.fengxin.controller;

import com.fengxin.dto.req.CouponTemplateSaveReqDTO;
import com.fengxin.service.CouponTemplateService;
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
 * @date 2024/10/16
 * @project feng-coupon
 * @description
 **/
@RestController
@RequiredArgsConstructor
@Tag (name = "优惠券模板管理")
public class CouponTemplateController {
    private final CouponTemplateService couponTemplateService;
    
    @Operation(summary = "商家创建优惠券模板")
    @PostMapping("/api/merchant-admin/coupon-template/create")
    public Result<Void> createCouponTemplate(@RequestBody CouponTemplateSaveReqDTO requestParam) {
        couponTemplateService.createCouponTemplate(requestParam);
        return Results.success();
    }

}
