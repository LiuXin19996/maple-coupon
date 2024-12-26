package com.fengxin.maplecoupon.auth.remote.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author FENGXIN
 * @date 2024/10/20
 * @project maple-coupon
 * @description 优惠券模板增加发行量请求参数实体
 **/
@Data
@Schema(description = "优惠券模板增加发行量请求参数实体")
public class CouponTemplateNumberReqDTO {
    /**
     * 优惠券模板id
     */
    @Schema(description = "优惠券模板id",
            example = "1810966706881941507",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponTemplateId;
    
    /**
     * 增加发行数量
     */
    @Schema(description = "增加发行数量",
            example = "100",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer number;

}
