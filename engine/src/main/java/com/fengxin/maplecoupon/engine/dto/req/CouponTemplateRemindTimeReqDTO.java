package com.fengxin.maplecoupon.engine.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author FENGXIN
 * @date 2024/10/30
 * @project feng-coupon
 * @description 设置提醒时间请求参数
 **/
@Data
@Schema(description = "设置提醒时间请求参数")
public class CouponTemplateRemindTimeReqDTO {
    /**
     * 优惠券模板id
     */
    @Schema(description = "优惠券模板id", example = "xxxxxx")
    @NotNull(message = "优惠券模板id不能为空")
    private String couponTemplateId;

    /**
     * 店铺编号
     */
    @Schema(description = "店铺编号", example = "1810714735922956666")
    @NotNull(message = "店铺编号不能为空")
    private String shopNumber;

    /**
     * 提醒方式
     */
    @Schema(description = "提醒方式", example = "0")
    @NotNull(message = "提醒方式不能为空")
    private Integer type;

    /**
     * 提醒时间，比如五分钟，十分钟，十五分钟
     */
    @Schema(description = "提醒时间", example = "5")
    @NotNull(message = "提醒时间不能为空")
    private Integer remindTime;
}
