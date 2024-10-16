package com.fengxin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengxin.common.constant.MerchantAdminRedisConstant;
import com.fengxin.common.context.UserContext;
import com.fengxin.common.enums.CouponTemplateStatusEnum;
import com.fengxin.common.enums.DiscountTargetEnum;
import com.fengxin.common.enums.DiscountTypeEnum;
import com.fengxin.dao.entity.CouponTemplateDO;
import com.fengxin.dao.mapper.CouponTemplateMapper;
import com.fengxin.dto.req.CouponTemplateSaveReqDTO;
import com.fengxin.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.exception.ClientException;
import com.fengxin.service.CouponTemplateService;
import com.fengxin.service.basic.chain.MerchantAdminChainContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fengxin.common.enums.ChainBizMarkEnum.MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券模板业务层
 **/
@Service
@RequiredArgsConstructor
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplateDO> implements CouponTemplateService  {
    private final CouponTemplateMapper couponTemplateMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final MerchantAdminChainContext merchantAdminChainContext;
    
    
    @Override
    public void createCouponTemplate (CouponTemplateSaveReqDTO requestParam) {
        // 使用责任链校验数据
        merchantAdminChainContext.handler (MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY.name (), requestParam);
        // 新增优惠券模板信息到数据库
        CouponTemplateDO couponTemplateDO = BeanUtil.toBean(requestParam, CouponTemplateDO.class);
        couponTemplateDO.setStatus(CouponTemplateStatusEnum.ACTIVE.getValue ());
        couponTemplateDO.setShopNumber(UserContext.getShopNumber());
        couponTemplateMapper.insert(couponTemplateDO);
        
        // 缓存预热：通过将数据库的记录序列化成 JSON 字符串放入 Redis 缓存
        CouponTemplateQueryRespDTO actualRespDTO = BeanUtil.toBean(couponTemplateDO, CouponTemplateQueryRespDTO.class);
        Map<String, Object> cacheTargetMap = BeanUtil.beanToMap(actualRespDTO, false, true);
        Map<String, String> actualCacheTargetMap = cacheTargetMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() != null ? entry.getValue().toString() : ""
                ));
        String couponTemplateCacheKey = String.format(MerchantAdminRedisConstant.COUPON_TEMPLATE_KEY, couponTemplateDO.getId());
        stringRedisTemplate.opsForHash().putAll(couponTemplateCacheKey, actualCacheTargetMap);
        
    }
}
