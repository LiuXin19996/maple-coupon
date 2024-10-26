package com.fengxin.maplecoupon.distribution.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fengxin.maplecoupon.distribution.dao.entity.UserCouponDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author FENGXIN
 * @date 2024/10/19
 * @project feng-coupon
 * @description 日志mapper
 **/
public interface UserCouponMapper extends BaseMapper<UserCouponDO> {
    
    void batchSaveUserCouponList(@Param ("userCouponList") List<UserCouponDO> userCouponList);
}
