package com.fengxin.maplecoupon.auth.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author FENGXIN
 * @date 2024/10/20
 * @project feng-coupon
 * @description 分页查询优惠券请求参数
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "分页查询优惠券请求参数")
public class CouponTemplatePageQueryReqDTO extends Page {
    /**
     * 优惠券名称
     */
    @Schema(description = "优惠券名称")
    private String name;
    
    /**
     * 优惠对象 0：商品专属 1：全店通用
     */
    @Schema(description = "优惠对象 0：商品专属 1：全店通用")
    private Integer target;
    
    /**
     * 优惠商品编码
     */
    @Schema(description = "优惠商品编码")
    private String goods;
    
    /**
     * 优惠类型 0：立减券 1：满减券 2：折扣券
     */
    @Schema(description = "优惠类型 0：立减券 1：满减券 2：折扣券")
    private Integer type;
    
}
