package com.fengxin.maplecoupon.engine.dto.req;

import com.fengxin.maplecoupon.engine.dto.req.CouponCreatePaymentGoodsReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券 创建付款请求参数
 *
 * @author fengxin
 * @date 2024-10-31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponCreatePaymentReqDTO {
    /**
     * 用户优惠券ID
     */
    @Schema(description = "用户优惠券ID")
    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;
    
    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    
    /**
     * 订单金额
     */
    @Schema(description = "订单金额")
    @NotNull(message = "订单金额不能为空")
    private BigDecimal orderAmount;
    
    /**
     * 折扣后金额
     */
    @Schema(description = "折扣后金额")
    @NotNull(message = "折扣后金额不能为空")
    private BigDecimal payableAmount;
    
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
    @NotNull(message = "商品集合不能为null,集合可为空")
    private List<CouponCreatePaymentGoodsReqDTO> goodsList;
}
