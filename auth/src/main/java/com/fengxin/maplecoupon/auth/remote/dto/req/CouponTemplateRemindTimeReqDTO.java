package com.fengxin.maplecoupon.auth.remote.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "优惠券模板id", example = "xxxxxx", required = true)
    private String couponTemplateId;

    /**
     * 店铺编号
     */
    @Schema(description = "店铺编号", example = "1810714735922956666", required = true)
    private String shopNumber;

    /**
     * 提醒方式
     */
    @Schema(description = "提醒方式", example = "0", required = true)
    private Integer type;

    /**
     * 提醒时间，比如五分钟，十分钟，十五分钟
     */
    @Schema(description = "提醒时间", example = "5", required = true)
    private Integer remindTime;
}
