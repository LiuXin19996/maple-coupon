package com.fengxin.maplecoupon.auth.remote;

import com.fengxin.maplecoupon.auth.config.OpenFeignConfiguration;
import com.fengxin.maplecoupon.auth.remote.dto.req.QueryCouponsReqDTO;
import com.fengxin.maplecoupon.auth.remote.dto.resp.QueryCouponsRespDTO;
import com.fengxin.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author FENGXIN
 */
@FeignClient(
        value = "MapleCoupon-settlement",
        configuration = OpenFeignConfiguration.class)
public interface MapleCouponSettlementRemoteService {

    @PostMapping("/api/settlement/coupon-query")
    Result<QueryCouponsRespDTO> listQueryCoupons (@RequestBody QueryCouponsReqDTO requestParam);
    
    @PostMapping("/api/settlement/coupon-query-sync")
    Result<QueryCouponsRespDTO> listQueryCouponsBySync (@RequestBody QueryCouponsReqDTO requestParam);
}
