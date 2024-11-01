package com.fengxin.maplecoupon.engine.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券流程 退款请求参数
 *
 * @author fengxin
 * @date 2024-10-31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponProcessRefundReqDTO {
    /**
     * 优惠券ID
     */
    @Schema(description = "优惠券ID", required = true)
    private Long couponId;

}
