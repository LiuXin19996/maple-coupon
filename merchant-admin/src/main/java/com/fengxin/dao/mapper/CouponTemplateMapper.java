package com.fengxin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengxin.dao.entity.CouponTemplateDO;
import org.apache.ibatis.annotations.Param;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券模板Mapper
 **/
public interface CouponTemplateMapper extends BaseMapper<CouponTemplateDO> {
    /**
     * 增加优惠券模板发行量
     *
     * @param shopNumber       店铺编号
     * @param couponTemplateId 优惠券模板 ID
     * @param number           增加发行数量
     */
    int increaseNumberCouponTemplate(@Param("shopNumber") Long shopNumber
            , @Param("couponTemplateId") String couponTemplateId
            , @Param("number") Integer number);
    
}
