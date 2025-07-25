package com.fengxin.maplecoupon.merchantadmin.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券模板实体
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_coupon_template")
public class CouponTemplateDO {
    /**
     * id
     */
    private Long id;
    
    /**
     * 店铺编号
     */
    private Long shopNumber;
    
    /**
     * 优惠券名称
     */
    private String name;
    
    /**
     * 优惠券来源 0：店铺券 1：平台券
     */
    private Integer source;
    
    /**
     * 优惠对象 0：商品专属 1：全店通用
     */
    private Integer target;
    
    /**
     * 优惠商品编码
     */
    private String goods;
    
    /**
     * 优惠类型 0：立减券 1：满减券 2：折扣券
     */
    private Integer type;
    
    /**
     * 有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validStartTime;
    
    /**
     * 有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validEndTime;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 领取规则
     */
    private String receiveRule;
    
    /**
     * 消耗规则
     */
    private String consumeRule;
    
    /**
     * 优惠券状态 0：生效中 1：已结束
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    
    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    
    /**
     * 删除标识 0：未删除 1：已删除
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

}
