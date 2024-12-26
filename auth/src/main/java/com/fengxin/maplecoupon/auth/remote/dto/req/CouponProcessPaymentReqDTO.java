package com.fengxin.maplecoupon.auth.remote.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券处理 付款请求参数实体
 *
 * @author fengxin
 * @date 2024-10-31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponProcessPaymentReqDTO {
    /**
     * 用户优惠券ID
     */
    @Schema(description = "优惠券ID", required = true)
    private Long couponId;

}
