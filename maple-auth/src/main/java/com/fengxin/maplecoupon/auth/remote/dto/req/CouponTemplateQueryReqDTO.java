package com.fengxin.maplecoupon.auth.remote.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author FENGXIN
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "优惠券模板查询请求参数实体")
public class CouponTemplateQueryReqDTO {

    /**
     * 店铺编号
     */
    @Schema(description = "店铺编号", example = "1810714735922956666")
    @NotNull(message = "店铺编号不能为空")
    private String shopNumber;

    /**
     * 优惠券模板id
     */
    @Schema(description = "优惠券模板id", example = "1810966706881941507")
    @NotNull(message = "优惠券模板id不能为空")
    private String couponTemplateId;
}
