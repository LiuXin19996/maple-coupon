package com.fengxin.maplecoupon.merchantadmin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTemplateNumberReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTemplatePageQueryReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.req.TerminateCouponTemplateReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.resp.CouponTemplatePageQueryRespDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTemplateSaveReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.idempotent.DuplicateSubmit;
import com.fengxin.maplecoupon.merchantadmin.service.CouponTemplateService;
import com.fengxin.web.Result;
import com.fengxin.web.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    
    @DuplicateSubmit
    @Operation(summary = "商家创建优惠券模板")
    @PostMapping("/api/merchant-admin/coupon-template/create")
    public Result<Void> createCouponTemplate(@RequestBody CouponTemplateSaveReqDTO requestParam) {
        couponTemplateService.createCouponTemplate(requestParam);
        return Results.success();
    }
    
    @Operation(summary = "分页查询优惠券模板")
    @GetMapping("/api/merchant-admin/coupon-template/page")
    public Result<IPage<CouponTemplatePageQueryRespDTO>> pageQueryCouponTemplate(CouponTemplatePageQueryReqDTO requestParam) {
        return Results.success(couponTemplateService.pageQueryCouponTemplate(requestParam));
    }
    
    @Operation(summary = "查询优惠券模板详情")
    @GetMapping("/api/merchant-admin/coupon-template/find")
    public Result<CouponTemplateQueryRespDTO> findCouponTemplate(String couponTemplateId) {
        return Results.success(couponTemplateService.findCouponTemplateById(couponTemplateId));
    }
    
    @Operation(summary = "增加优惠券模板发行量")
    @DuplicateSubmit(message = "请勿短时间内重复增加优惠券发行量")
    @PostMapping("/api/merchant-admin/coupon-template/increase-number")
    public Result<Void> increaseNumberCouponTemplate(@RequestBody CouponTemplateNumberReqDTO requestParam) {
        couponTemplateService.increaseNumberCouponTemplate(requestParam);
        return Results.success();
    }
    
    @Operation(summary = "结束优惠券模板")
    @PostMapping("/api/merchant-admin/coupon-template/terminate")
    public Result<Void> terminateCouponTemplate(@RequestBody TerminateCouponTemplateReqDTO requestParam) {
        couponTemplateService.terminateCouponTemplate(requestParam);
        return Results.success();
    }
    
    @Operation(summary = "删除优惠券模板")
    @DeleteMapping("/api/merchant-admin/coupon-template/delete")
    public Result<Void> deleteCouponTemplate(@RequestParam("couponTemplateId") String couponTemplateId) {
        couponTemplateService.deleteCouponTemplate(couponTemplateId);
        return Results.success();
    }
    
}
