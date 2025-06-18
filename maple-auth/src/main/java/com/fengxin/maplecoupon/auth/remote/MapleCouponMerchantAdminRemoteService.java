package com.fengxin.maplecoupon.auth.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fengxin.maplecoupon.auth.config.OpenFeignConfiguration;
import com.fengxin.maplecoupon.auth.remote.dto.req.*;
import com.fengxin.maplecoupon.auth.remote.dto.resp.CouponTemplatePageQueryRespDTO;
import com.fengxin.maplecoupon.auth.remote.dto.resp.CouponTemplateQueryRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.fengxin.web.Result;

/**
 * @author FENGXIN
 */
@FeignClient(
        value = "MapleCoupon-merchant-admin",
        configuration = OpenFeignConfiguration.class)
public interface MapleCouponMerchantAdminRemoteService {
    
    @PostMapping("/api/merchant-admin/coupon-task/create")
    Result<Void> createCouponTask(@RequestBody CouponTaskCreateReqDTO requestParam);
    
    @PostMapping("/api/merchant-admin/coupon-template/create")
    Result<Void> createCouponTemplate (@RequestBody CouponTemplateSaveReqDTO requestParam);
    
    @GetMapping("/api/merchant-admin/coupon-template/page")
    Result<Page<CouponTemplatePageQueryRespDTO>> pageQueryCouponTemplate (
            @RequestParam("name") String name,
            @RequestParam("target") Integer target,
            @RequestParam("goods") String goods,
            @RequestParam("type") Integer type,
            @RequestParam("current") Long current,
            @RequestParam("size") Long size
    );
    
    @GetMapping("/api/merchant-admin/coupon-template/find")
    Result<CouponTemplateQueryRespDTO> findCouponTemplate (@RequestParam("couponTemplateId") String couponTemplateId);
    
    @PostMapping("/api/merchant-admin/coupon-template/increase-number")
    Result<Void> increaseNumberCouponTemplate (@RequestBody CouponTemplateNumberReqDTO requestParam);
    
    @PostMapping("/api/merchant-admin/coupon-template/terminate")
    Result<Void> terminateCouponTemplate (@RequestBody TerminateCouponTemplateReqDTO requestParam);
    
    @DeleteMapping("/api/merchant-admin/coupon-template/delete")
    Result<Void> deleteCouponTemplate (@RequestParam("couponTemplateId") String couponTemplateId);
}
