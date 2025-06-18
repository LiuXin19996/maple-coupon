package com.fengxin.maplecoupon.distribution.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTemplateDO;
import org.apache.ibatis.annotations.Param;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券模板Mapper
 **/
public interface CouponTemplateMapper extends BaseMapper<CouponTemplateDO> {
    /**
     * 扣减优惠券模板发行量
     *
     * @param shopNumber       店铺编号
     * @param couponTemplateId 优惠券模板 ID
     * @param decrementStock           扣减优惠券模板发行量
     */
    int decrementCouponTemplateStock(@Param("shopNumber") Long shopNumber
            , @Param("couponTemplateId") Long couponTemplateId
            , @Param("decrementStock") Integer decrementStock);
    
}
