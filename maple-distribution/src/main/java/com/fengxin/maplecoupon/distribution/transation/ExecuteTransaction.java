package com.fengxin.maplecoupon.distribution.transation;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fengxin.maplecoupon.distribution.dao.entity.UserCouponDO;
import com.fengxin.maplecoupon.distribution.dao.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author FENGXIN
 * @date 2024/10/26
 * @project feng-coupon
 * @description
 **/
@Component
@RequiredArgsConstructor
public class ExecuteTransaction {
    private final UserCouponMapper userCouponMapper;
    /**
     * 用户已经获取优惠券
     * @description 如果第一次批量保存数据库失败后，批量保存的一些记录会在事务缓冲区中，如果在同一个事务下，会查到数据库中本不存在的记录。我们保存用户已领取优惠券的失败记录，需要是数据库中真实存在的，所以不使用事务的方式查询
     * @param userCouponDO 用户优惠券
     * @return {@code Boolean }
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    public Boolean hasUserGetCoupon(UserCouponDO userCouponDO) {
        LambdaQueryWrapper<UserCouponDO> queryWrapper = new LambdaQueryWrapper <UserCouponDO> ()
                .eq (UserCouponDO::getUserId, userCouponDO.getUserId ())
                .eq (UserCouponDO::getCouponTemplateId, userCouponDO.getCouponTemplateId ());
        return ObjectUtil.isNotEmpty (userCouponMapper.selectOne(queryWrapper));
    }
}
