package com.fengxin.maplecoupon.auth.remote.dto.req;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project maple-coupon
 * @description 新增优惠券模板参数
 **/
@Data
@Schema(description = "优惠券模板新增参数")
public class CouponTemplateSaveReqDTO {
    /**
     * 优惠券名称
     */
    @Schema(description = "优惠券名称",
            example = "用户下单满10减3特大优惠")
    @NotNull(message = "优惠券名称不能为空")
    private String name;
    
    /**
     * 优惠券来源 0：店铺券 1：平台券
     */
    @Schema(description = "优惠券来源 0：店铺券 1：平台券",
            example = "0")
    @NotNull(message = "优惠券来源不能为空")
    private Integer source;
    
    /**
     * 优惠对象 0：商品专属 1：全店通用
     */
    @Schema(description = "优惠对象 0：商品专属 1：全店通用",
            example = "1")
    @NotNull(message = "优惠对象不能为空")
    private Integer target;
    
    /**
     * 优惠商品编码
     */
    @Schema(description = "优惠商品编码")
    @NotNull(message = "优惠商品编码不能为空")
    private String goods;
    
    /**
     * 优惠类型 0：立减券 1：满减券 2：折扣券
     */
    @Schema(description = "优惠类型 0：立减券 1：满减券 2：折扣券",
            example = "0")
    @NotNull(message = "优惠类型不能为空")
    private Integer type;
    
    /**
     * 有效期开始时间
     */
    @Schema(description = "有效期开始时间",
            example = "2024-11-08 08:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validStartTime;
    
    /**
     * 有效期结束时间
     */
    @Schema(description = "有效期结束时间",
            example = "2025-11-18 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validEndTime;
    
    /**
     * 库存
     */
    @Schema(description = "库存",
            example = "200")
    @NotNull(message = "库存不能为空")
    private Integer stock;
    
    /**
     * 领取规则
     */
    @Schema(description = "领取规则",
            example = "{\"limitPerPerson\":1,\"usageInstructions\":\"3\"}")
    @NotNull(message = "领取规则不能为空")
    private String receiveRule;
    
    /**
     * 消耗规则
     */
    @Schema(description = "消耗规则",
            example = "{\"termsOfUse\":10,\"maximumDiscountAmount\":3,\"discountRate\":0.8,\"explanationOfUnmetConditions\":\"3\",\"validityPeriod\":\"48\"}")
    @NotNull(message = "消耗规则不能为空")
    private String consumeRule;
    
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
