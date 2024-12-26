package com.fengxin.maplecoupon.auth.remote.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author FENGXIN
 * @date 2024/10/28
 * @project feng-coupon
 * @description
 **/
@Data
@Schema(description = "兑换优惠券请求参数实体")
public class CouponTemplateRedeemReqDTO {

    /**
     * 券来源 0：领券中心 1：平台发放 2：店铺领取
     */
    @Schema(description = "券来源", example = "0")
    @NotNull(message = "券来源不能为空")
    private Integer source;

    /**
     * 店铺编号
     */
    @Schema(description = "店铺编号", example = "1810714735922956666")
    @NotBlank(message = "店铺编号不能为空")
    private String shopNumber;

    /**
     * 优惠券模板id
     */
    @Schema(description = "优惠券模板id", example = "1810966706881941507")
    @NotBlank(message = "优惠券模板id不能为空")
    private String couponTemplateId;
}
