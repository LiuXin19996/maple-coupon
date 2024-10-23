package com.fengxin.maplecoupon.engine.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengxin.maplecoupon.engine.common.context.UserContext;
import com.fengxin.maplecoupon.engine.common.enums.CouponTemplateStatusEnum;
import com.fengxin.maplecoupon.engine.dao.mapper.CouponTemplateMapper;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateQueryReqDTO;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.maplecoupon.engine.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.engine.service.CouponTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券模板业务层
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper,CouponTemplateDO> implements CouponTemplateService {
    private final CouponTemplateMapper couponTemplateMapper;
    private final StringRedisTemplate stringRedisTemplate;
    
    @Override
    public CouponTemplateQueryRespDTO findCouponTemplateById (CouponTemplateQueryReqDTO requestParam) {
        LambdaQueryWrapper<CouponTemplateDO> queryWrapper = new LambdaQueryWrapper<CouponTemplateDO>()
                .eq (CouponTemplateDO::getId,Long.valueOf (requestParam.getCouponTemplateId ()))
                .eq (CouponTemplateDO::getStatus,CouponTemplateStatusEnum.ACTIVE.getValue ())
                .eq (CouponTemplateDO::getShopNumber,Long.valueOf (requestParam.getShopNumber ()));
        CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne (queryWrapper);
        return BeanUtil.toBean(couponTemplateDO, CouponTemplateQueryRespDTO.class);
    }
    
}
