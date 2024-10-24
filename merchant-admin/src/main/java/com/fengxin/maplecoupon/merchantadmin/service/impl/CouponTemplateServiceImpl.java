package com.fengxin.maplecoupon.merchantadmin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.fengxin.maplecoupon.merchantadmin.common.constant.MerchantAdminRedisConstant;
import com.fengxin.maplecoupon.merchantadmin.common.context.UserContext;
import com.fengxin.maplecoupon.merchantadmin.common.enums.CouponTemplateStatusEnum;
import com.fengxin.maplecoupon.merchantadmin.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.merchantadmin.dao.mapper.CouponTemplateMapper;
import com.fengxin.maplecoupon.merchantadmin.dto.mq.CouponTemplateDelayExecuteEvent;
import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTemplateNumberReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTemplatePageQueryReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTemplateSaveReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.resp.CouponTemplatePageQueryRespDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.exception.ClientException;
import com.fengxin.exception.ServiceException;
import com.fengxin.maplecoupon.merchantadmin.mq.producer.CouponTemplateDelayTerminalStatusProducer;
import com.fengxin.maplecoupon.merchantadmin.service.CouponTemplateService;
import com.fengxin.maplecoupon.merchantadmin.service.basic.chain.MerchantAdminChainContext;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fengxin.maplecoupon.merchantadmin.common.enums.ChainBizMarkEnum.MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券模板业务层
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplateDO> implements CouponTemplateService  {
    private final CouponTemplateMapper couponTemplateMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final MerchantAdminChainContext merchantAdminChainContext;
    private final CouponTemplateDelayTerminalStatusProducer couponTemplateDelayTerminalStatusProducer;
    private final RBloomFilter<String> couponTemplateQueryBloomFilter;
    private String merchantMQTopic = "merchant-admin-topic";
    
    
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
        
        // 添加入布隆过滤器
        couponTemplateQueryBloomFilter.add (String.valueOf(couponTemplateDO.getId()));
        
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
        stringRedisTemplate.expireAt (couponTemplateCacheKey, requestParam.getValidEndTime ());
        
        /*
          优惠券有效期结束自动设置数据库优惠券状态 使用RocketMQ任意延时消息实现
          缓存到期自动删除
         */
        // 设置消息体
        CouponTemplateDelayExecuteEvent couponTemplateDelayExecuteEvent = CouponTemplateDelayExecuteEvent.builder ()
                .shopNumber (couponTemplateDO.getShopNumber ())
                .couponTemplateId (couponTemplateDO.getId ())
                .delayTime (requestParam.getValidEndTime ().getTime ())
                .build ();
        couponTemplateDelayTerminalStatusProducer.sendMessage (couponTemplateDelayExecuteEvent);
    }
    
    @Override
    public CouponTemplateQueryRespDTO findCouponTemplateById (String couponTemplateId) {
        LambdaQueryWrapper<CouponTemplateDO> queryWrapper = new LambdaQueryWrapper<CouponTemplateDO>()
                .eq(CouponTemplateDO::getShopNumber,UserContext.getShopNumber())
                .eq(CouponTemplateDO::getId,couponTemplateId);
        CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne (queryWrapper);
        return BeanUtil.toBean(couponTemplateDO, CouponTemplateQueryRespDTO.class);
    }
    
    @Override
    @LogRecord (
            success = "增加发行量：{{#requestParam.number}}",
            type = "CouponTemplate",
            bizNo = "#{{#requestParam.couponTemplateId}}"
    )
    public void increaseNumberCouponTemplate (CouponTemplateNumberReqDTO requestParam) {
        // 检验用户横向越权
        LambdaQueryWrapper<CouponTemplateDO> queryWrapper = new LambdaQueryWrapper<CouponTemplateDO> ()
                .eq(CouponTemplateDO::getId,requestParam.getCouponTemplateId())
                .eq (CouponTemplateDO::getShopNumber,UserContext.getShopNumber());
        CouponTemplateDO selectOne = couponTemplateMapper.selectOne (queryWrapper);
        if (ObjectUtil.isNull(selectOne)) {
            // 基本判定用户越权 可以上报中心进行风控
            throw new ClientException ("优惠券模板异常，请检查您是否拥有此优惠券");
        }
        // 校验优惠券状态是否可用
        if(ObjectUtil.equals (selectOne.getStatus(), CouponTemplateStatusEnum.ENDED.getValue ())){
            throw new ClientException ("优惠券已截止");
        }
        // 记录原始数据
        LogRecordContext.putVariable ("originalData", JSON.toJSONString(selectOne));
        // 新增数据
        int increase = baseMapper.increaseNumberCouponTemplate (UserContext.getShopNumber () , requestParam.getCouponTemplateId () , requestParam.getNumber ());
        if (!SqlHelper.retBool (increase)) {
            throw new ServiceException ("优惠券发行量新增失败");
        }
        // 设置redis缓存发行量
        String couponTemplateCacheKey = String.format(MerchantAdminRedisConstant.COUPON_TEMPLATE_KEY, selectOne.getId ());
        stringRedisTemplate.opsForHash ().increment (couponTemplateCacheKey, "stock", requestParam.getNumber ());
    }
    
    @Override
    @LogRecord (
            success = "结束优惠券",
            type = "CouponTemplate",
            bizNo = "#{{couponTemplateId}}"
    )
    public void terminateCouponTemplate (String couponTemplateId) {
        // 1.校验用户横向越权
        LambdaQueryWrapper<CouponTemplateDO> queryWrapper = new LambdaQueryWrapper<CouponTemplateDO> ()
                .eq(CouponTemplateDO::getId,couponTemplateId)
                .eq (CouponTemplateDO::getShopNumber,UserContext.getShopNumber());
        CouponTemplateDO selectOne = couponTemplateMapper.selectOne (queryWrapper);
        if (ObjectUtil.isNull(selectOne)) {
            // 基本判定用户越权 可以上报中心进行风控
            throw new ClientException ("优惠券模板异常，请检查您是否拥有此优惠券");
        }
        // 2.校验优惠券状态是否可用
        if(ObjectUtil.equals (selectOne.getStatus(), CouponTemplateStatusEnum.ENDED.getValue ())){
            throw new ClientException ("优惠券已截止");
        }
        // 3.记录原始数据
        LogRecordContext.putVariable ("originalData", JSON.toJSONString(selectOne));
        
        // 5.设置缓存优惠券结束
        String couponTemplateCacheKey = String.format(MerchantAdminRedisConstant.COUPON_TEMPLATE_KEY, selectOne);
        stringRedisTemplate.opsForHash ().put (couponTemplateCacheKey, "status",String.valueOf (CouponTemplateStatusEnum.ENDED.getValue ()));
        // 4.修改优惠券结束状态
        LambdaUpdateWrapper<CouponTemplateDO> updateWrapper = new LambdaUpdateWrapper<CouponTemplateDO> ()
                .eq(CouponTemplateDO::getId,couponTemplateId)
                .eq (CouponTemplateDO::getShopNumber,UserContext.getShopNumber());
        CouponTemplateDO build = CouponTemplateDO.builder ()
                .status (CouponTemplateStatusEnum.ENDED.getValue ())
                .build ();
        int update = couponTemplateMapper.update (build , updateWrapper);
        if (!SqlHelper.retBool (update)) {
            throw new ServiceException ("优惠券结束失败");
        }
        
    }
    
    @Override
    public IPage<CouponTemplatePageQueryRespDTO> pageQueryCouponTemplate (CouponTemplatePageQueryReqDTO requestParam) {
        LambdaQueryWrapper<CouponTemplateDO> queryWrapper = new LambdaQueryWrapper<CouponTemplateDO> ()
                .eq(CouponTemplateDO::getShopNumber,UserContext.getShopNumber())
                .like (StrUtil.isNotBlank (requestParam.getName ()),CouponTemplateDO::getName,requestParam.getName())
                .like (StrUtil.isNotBlank (requestParam.getGoods ()),CouponTemplateDO::getGoods,requestParam.getGoods ())
                .eq(Objects.nonNull(requestParam.getType()), CouponTemplateDO::getType, requestParam.getType())
                .eq(Objects.nonNull(requestParam.getTarget()), CouponTemplateDO::getTarget, requestParam.getTarget());
        IPage<CouponTemplateDO> selectPage = couponTemplateMapper.selectPage(requestParam, queryWrapper);
        // 转换成响应实体
        return selectPage.convert (ea -> BeanUtil.toBean(ea,CouponTemplatePageQueryRespDTO.class));
    }
    
    
}
