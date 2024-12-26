package com.fengxin.maplecoupon.auth.remote;

import com.fengxin.maplecoupon.auth.config.OpenFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author FENGXIN
 */
@FeignClient(
        value = "MapleCoupon-engine",
        configuration = OpenFeignConfiguration.class)
public interface MapleCouponEngineRemoteService {

}
