package com.fengxin.maplecoupon.auth.remote.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 优惠券模板提醒取消请求参数
 *
 * @author fengxin
 * @date 2024-10-31
 */
@Data
@Schema(description = "取消优惠券预约抢券提醒参数实体")
public class CouponTemplateRemindCancelReqDTO {

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

    /**
     * 提醒时间，比如五分钟，十分钟，十五分钟
     */
    @Schema(description = "提醒时间", example = "15")
    @NotNull(message = "提醒时间不能为空")
    private Integer remindTime;

    /**
     * 提醒方式
     */
    @Schema(description = "提醒方式", example = "0")
    @NotNull(message = "提醒方式不能为空")
    private Integer type;
}
