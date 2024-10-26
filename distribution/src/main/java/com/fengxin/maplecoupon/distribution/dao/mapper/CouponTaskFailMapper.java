package com.fengxin.maplecoupon.distribution.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTaskFailDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author FENGXIN
 * @date 2024/10/25
 * @project feng-coupon
 * @description 分发优惠券失败mapper
 **/
public interface CouponTaskFailMapper extends BaseMapper<CouponTaskFailDO> {
    
    void insertBatch(@Param ("couponTaskFailDOList") List<CouponTaskFailDO> couponTaskFailDOList);
}
