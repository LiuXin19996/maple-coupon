package com.fengxin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengxin.common.constant.MerchantAdminRedisConstant;
import com.fengxin.common.context.UserContext;
import com.fengxin.common.enums.CouponTemplateStatusEnum;
import com.fengxin.dao.entity.CouponTemplateDO;
import com.fengxin.dao.mapper.CouponTemplateMapper;
import com.fengxin.dto.req.CouponTemplateSaveReqDTO;
import com.fengxin.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.service.CouponTemplateService;
import com.fengxin.service.basic.chain.MerchantAdminChainContext;
import com.fengxin.util.MerchantAdminUtils;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
    
    // 日志记录
    @LogRecord (
            // 方法执行成功后的日志模板
            success = """
                创建优惠券：{{#requestParam.name}}， \
                优惠对象：{COMMON_PARSE_DISCOUNT{'DiscountTargetEnum' + '_' + #requestParam.target}}， \
                优惠类型：{COMMON_PARSE_DISCOUNT{'DiscountTypeEnum' + '_' + #requestParam.type}}， \
                库存数量：{{#requestParam.stock}}， \
                优惠商品编码：{{#requestParam.goods}}， \
                有效期开始时间：{{#requestParam.validStartTime}}， \
                有效期结束时间：{{#requestParam.validEndTime}}， \
                领取规则：{{#requestParam.receiveRule}}， \
                消耗规则：{{#requestParam.consumeRule}};
                """,
            // 操作日志的类型 如商品类型 优惠券等
            type = "CouponTemplate",
            // 一条日志绑定的业务标识 这里是优惠券id
            bizNo = "{{#bizNo}}",
            // 额外信息
            extra = "{{#requestParam.toString()}}"
    )
    @Override
    public void createCouponTemplate (CouponTemplateSaveReqDTO requestParam) {
        
        // 使用责任链校验数据
        merchantAdminChainContext.handler (MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY.name (), requestParam);
        // 新增优惠券模板信息到数据库
        CouponTemplateDO couponTemplateDO = BeanUtil.toBean(requestParam, CouponTemplateDO.class);
        couponTemplateDO.setStatus(CouponTemplateStatusEnum.ACTIVE.getValue ());
        couponTemplateDO.setShopNumber(UserContext.getShopNumber());
        couponTemplateMapper.insert(couponTemplateDO);
        
        // 因为模板 ID 是运行中生成的，@LogRecord 默认拿不到，所以我们需要手动设置
        LogRecordContext.putVariable ("bizNo",couponTemplateDO.getId ());
        
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
        stringRedisTemplate.expireAt (couponTemplateCacheKey, MerchantAdminUtils.formatDate (requestParam.getValidEndTime ()));
    }
}
