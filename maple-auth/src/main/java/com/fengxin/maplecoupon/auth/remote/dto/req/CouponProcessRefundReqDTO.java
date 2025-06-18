package com.fengxin.maplecoupon.auth.remote.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
    @Schema(description = "优惠券ID")
    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;

}
