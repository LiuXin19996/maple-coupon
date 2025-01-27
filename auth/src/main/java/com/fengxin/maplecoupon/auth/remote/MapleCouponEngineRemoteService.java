package com.fengxin.maplecoupon.auth.remote;

import com.fengxin.maplecoupon.auth.config.OpenFeignConfiguration;
import com.fengxin.maplecoupon.auth.remote.dto.req.*;
import com.fengxin.maplecoupon.auth.remote.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.maplecoupon.auth.remote.dto.resp.CouponTemplateRemindQueryRespDTO;
import com.fengxin.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author FENGXIN
 */
@FeignClient(
        value = "MapleCoupon-engine",
        configuration = OpenFeignConfiguration.class)
public interface MapleCouponEngineRemoteService {

    @GetMapping("/api/engine/coupon-template/query")
    Result<CouponTemplateQueryRespDTO> findCouponTemplate (
            @RequestParam("couponTemplateId") String couponTemplateId,
            @RequestParam("shopNumber") String shopNumber);
    
    @PostMapping("/api/engine/user-coupon/redeem")
    Result<Void> redeemUserCoupon (@RequestBody CouponTemplateRedeemReqDTO requestParam);
    
    @PostMapping("/api/engine/coupon-template-remind/create")
    Result<Void> createCouponRemind (@RequestBody CouponTemplateRemindTimeReqDTO requestParam);
    
    @GetMapping("/api/engine/coupon-template-remind/list")
    Result<List<CouponTemplateRemindQueryRespDTO>> listCouponRemind ();
    
    @PostMapping("/api/engine/coupon-template-remind/cancel")
    Result<Void> cancelCouponRemind (@RequestBody CouponTemplateRemindCancelReqDTO requestParam);
    
    @PostMapping("/api/engine/user-coupon/create-payment-record")
    Result<Void> createPaymentRecord (@RequestBody CouponCreatePaymentReqDTO requestParam);
    
    @PostMapping("/api/engine/user-coupon/process-payment")
    Result<Void> processPayment (@RequestBody CouponProcessPaymentReqDTO requestParam);
    
    @PostMapping("/api/engine/user-coupon/process-refund")
    Result<Void> processRefund (@RequestBody CouponProcessRefundReqDTO requestParam);
}
