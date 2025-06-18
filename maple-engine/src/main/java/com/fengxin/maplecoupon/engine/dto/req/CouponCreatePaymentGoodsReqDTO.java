package com.fengxin.maplecoupon.engine.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 优惠券 付款商品请求参数
 *
 * @author fengxin
 * @date 2024-10-31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponCreatePaymentGoodsReqDTO {
    /**
     * 商品编号
     */
    @Schema(description = "商品编号")
    private String goodsNumber;
    
    /**
     * 商品价格
     */
    @Schema(description = "商品价格")
    private BigDecimal goodsAmount;
    
    /**
     * 商品折扣后金额
     */
    @Schema(description = "商品折扣后金额")
    private BigDecimal goodsPayableAmount;
    
}
