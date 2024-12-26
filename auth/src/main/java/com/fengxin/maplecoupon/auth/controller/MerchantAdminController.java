package com.fengxin.maplecoupon.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengxin.idempotent.DuplicateSubmit;
import com.fengxin.maplecoupon.auth.remote.MapleCouponMerchantAdminRemoteService;
import com.fengxin.maplecoupon.auth.remote.dto.req.*;
import com.fengxin.maplecoupon.auth.remote.dto.resp.CouponTemplatePageQueryRespDTO;
import com.fengxin.maplecoupon.auth.remote.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author FENGXIN
 */
@RestController
@Tag(name = "商家后管")
@RequiredArgsConstructor
public class MerchantAdminController {
    private final MapleCouponMerchantAdminRemoteService mapleCouponMerchantAdminRemoteService;
    
    @Operation(summary = "创建优惠券推送任务")
    @DuplicateSubmit(message = "请勿短时间内重复提交优惠券推送任务")
    @PostMapping("/api/auth/merchant-admin/coupon-task/create")
    public Result<Void> createCouponTask(@RequestBody CouponTaskCreateReqDTO requestParam){
        return mapleCouponMerchantAdminRemoteService.createCouponTask (requestParam);
    }
    
    @DuplicateSubmit
    @Operation(summary = "商家创建优惠券模板")
    @PostMapping("/api/auth/merchant-admin/coupon-template/create")
    Result<Void> createCouponTemplate (@RequestBody CouponTemplateSaveReqDTO requestParam){
        return mapleCouponMerchantAdminRemoteService.createCouponTemplate (requestParam);
    }
    
    @Operation(summary = "分页查询优惠券模板")
    @GetMapping("/api/auth/merchant-admin/coupon-template/page")
    Result<Page<CouponTemplatePageQueryRespDTO>> pageQueryCouponTemplate (CouponTemplatePageQueryReqDTO requestParam){
        return mapleCouponMerchantAdminRemoteService.pageQueryCouponTemplate (requestParam);
    }
    
    @Operation(summary = "查询优惠券模板详情")
    @GetMapping("/api/auth/merchant-admin/coupon-template/find")
    Result<CouponTemplateQueryRespDTO> findCouponTemplate ( String couponTemplateId){
        return mapleCouponMerchantAdminRemoteService.findCouponTemplate (couponTemplateId);
    }
    
    @Operation(summary = "增加优惠券模板发行量")
    @DuplicateSubmit(message = "请勿短时间内重复增加优惠券发行量")
    @PostMapping("/api/auth/merchant-admin/coupon-template/increase-number")
    Result<Void> increaseNumberCouponTemplate (@RequestBody CouponTemplateNumberReqDTO requestParam){
        return mapleCouponMerchantAdminRemoteService.increaseNumberCouponTemplate (requestParam);
    }
    
    @Operation(summary = "结束优惠券模板")
    @PostMapping("/api/auth/merchant-admin/coupon-template/terminate")
    Result<Void> terminateCouponTemplate (@RequestBody TerminateCouponTemplateReqDTO requestParam){
        return mapleCouponMerchantAdminRemoteService.terminateCouponTemplate (requestParam);
    }
}
