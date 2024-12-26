package com.fengxin.maplecoupon.merchantadmin.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 终止优惠券模板 req DTO
 *
 * @author fengxin
 * @date 2024-12-21
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TerminateCouponTemplateReqDTO {
    @Schema(description = "优惠券结束id",
            example = "1870479981408038914")
    @NotNull(message = "优惠券结束id不能为空")
    private String couponTemplateId;
}
