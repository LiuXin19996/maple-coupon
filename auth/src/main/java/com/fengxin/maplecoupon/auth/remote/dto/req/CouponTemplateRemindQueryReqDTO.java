package com.fengxin.maplecoupon.auth.remote.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券模板 remind query req DTO
 *
 * @author fengxin
 * @date 2024-10-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询优惠券预约抢券提醒参数实体")
public class CouponTemplateRemindQueryReqDTO {

    /**
     * 用户id
     */
    @Schema(description = "用户id", example = "1810518709471555585")
    @NotNull(message = "用户id不能为空")
    private String userId;
}
