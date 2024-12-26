package com.fengxin.maplecoupon.auth.remote.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 查询优惠券集合请求参数
 *
 * @author fengxin
 * @date 2024-11-02
 */
@Data
@Schema(description = "查询用户优惠券请求参数")
public class QueryCouponsReqDTO {

    /**
     * 订单金额
     */
    @Schema(description = "订单金额")
    @NotNull(message = "订单金额不能为空")
    private BigDecimal orderAmount;

    /**
     * 店铺编号
     */
    @Schema(description = "店铺编号", example = "1810714735922956666")
    @NotNull(message = "店铺编号不能为空")
    private String shopNumber;

    /**
     * 商品集合
     */
    @Schema(description = "商品集合")
    @NotNull(message = "商品集合不能为空")
    private List<QueryCouponGoodsReqDTO> goodsList;
}