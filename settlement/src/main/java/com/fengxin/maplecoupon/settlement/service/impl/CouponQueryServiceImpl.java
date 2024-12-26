package com.fengxin.maplecoupon.settlement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fengxin.config.RedisDistributedProperties;
import com.fengxin.exception.ClientException;
import com.fengxin.maplecoupon.settlement.common.context.UserContext;
import com.fengxin.maplecoupon.settlement.dto.req.QueryCouponGoodsReqDTO;
import com.fengxin.maplecoupon.settlement.dto.req.QueryCouponsReqDTO;
import com.fengxin.maplecoupon.settlement.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.maplecoupon.settlement.dto.resp.QueryCouponsDetailRespDTO;
import com.fengxin.maplecoupon.settlement.dto.resp.QueryCouponsRespDTO;
import com.fengxin.maplecoupon.settlement.service.CouponQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fengxin.maplecoupon.settlement.common.constant.EngineRedisConstant.COUPON_TEMPLATE_KEY;
import static com.fengxin.maplecoupon.settlement.common.constant.EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY;

/**
 * 结算---->可用 / 不可用优惠券查询服务实现
 *
 * @author fengxin
 * @date 2024-11-02
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CouponQueryServiceImpl implements CouponQueryService {
    private final RedisDistributedProperties redisDistributedProperties;
    private final StringRedisTemplate stringRedisTemplate;
    // 在本次的业务场景中，属于是 CPU 密集型任务，设置 CPU 的核心数即可
    private final ExecutorService executorService = new ThreadPoolExecutor (
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            9999,
            TimeUnit.SECONDS,
            new SynchronousQueue<> (),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    
    @Override
    public QueryCouponsRespDTO listQueryUserCoupons (QueryCouponsReqDTO requestParam) {
        // 查询缓存优惠券id
        Set<String> cacheCouponTemplateIdList = stringRedisTemplate.opsForZSet ().range (
                String.format (USER_COUPON_TEMPLATE_LIST_KEY , UserContext.getUserId ()) , 0 , -1);
        if (ObjectUtil.isEmpty (cacheCouponTemplateIdList)){
            return null;
        }
        // 根据优惠券id构建缓存优惠券key
        List<String> cacheCouponTemplateKeyList = cacheCouponTemplateIdList.stream ()
                .map (each -> StrUtil.split (each , "_").get (0))
                .map (each -> redisDistributedProperties.getPrefix() + String.format (COUPON_TEMPLATE_KEY , each))
                .toList ();
        // 使用Pipeline批量查询缓存优惠券json
        List<Object> couponTemplateList = stringRedisTemplate.executePipelined ((RedisCallback<String>) connection -> {
            cacheCouponTemplateKeyList.forEach (each -> connection.hashCommands ().hGetAll (each.getBytes ()));
            // 结束回调函数
            return null;
        });
        // 转换实体
        List<CouponTemplateQueryRespDTO> couponTemplateQueryRespDTOList = couponTemplateList.stream()
                .map(item -> BeanUtil.toBean (item,CouponTemplateQueryRespDTO.class))
                .toList ();
        // 按照商品券和店铺券分类 方便计算折扣
        Map<Boolean, List<CouponTemplateQueryRespDTO>> couponTemplateByGoods = couponTemplateQueryRespDTOList.stream ()
                .collect (Collectors.partitioningBy (coupon -> StrUtil.isEmpty (coupon.getGoods ())));
        List<CouponTemplateQueryRespDTO> emptyGoodsList = couponTemplateByGoods.get (true);
        List<CouponTemplateQueryRespDTO> hasGoodsList = couponTemplateByGoods.get (false);
        // 计算店铺券折扣金额
        List<QueryCouponsDetailRespDTO> availableCouponList = Collections.synchronizedList (new ArrayList<> ());
        List<QueryCouponsDetailRespDTO> notAvailableCouponList = Collections.synchronizedList (new ArrayList<> ());
        
        // 计算店铺券
        CompletableFuture<Void> emptyGoodsTasks  = CompletableFuture.allOf (
            emptyGoodsList.stream ().map (each -> CompletableFuture.runAsync (() -> {
                QueryCouponsDetailRespDTO couponsDetailRespDTO = BeanUtil.toBean (each , QueryCouponsDetailRespDTO.class);
                JSONObject consumeRule = JSON.parseObject (couponsDetailRespDTO.getConsumeRule ());
                calculateCouponTemplateAmount (couponsDetailRespDTO , consumeRule , availableCouponList , notAvailableCouponList , requestParam.getOrderAmount ());
            } , executorService).orTimeout (10 , TimeUnit.SECONDS)).toArray (CompletableFuture[]::new)
        );
        
        // 计算商品券
        CompletableFuture<Void> notEmptyGoodsTasks = CompletableFuture.allOf (
            hasGoodsList.stream ().map (each -> CompletableFuture.runAsync (() -> {
                Map<String, QueryCouponGoodsReqDTO> goodsMap = requestParam.getGoodsList ().stream ()
                .collect (Collectors.toMap (QueryCouponGoodsReqDTO::getGoodsNumber ,
                    // 如果遇到重复的商品编号，保留第一个出现的商品对象
                    Function.identity () ,
                    (exist , replace) -> exist));
                QueryCouponsDetailRespDTO couponsDetailRespDTO = BeanUtil.toBean (each , QueryCouponsDetailRespDTO.class);
                JSONObject consumeRule = JSON.parseObject (couponsDetailRespDTO.getConsumeRule ());
                // 从请求参数里的商品编号校验是否有该商品的优惠券可用
                QueryCouponGoodsReqDTO couponGoodsReqDTO = goodsMap.get (each.getGoods ());
                if (ObjectUtil.isNull (couponGoodsReqDTO)) {
                    notAvailableCouponList.add (couponsDetailRespDTO);
                }
                calculateCouponTemplateAmount (couponsDetailRespDTO , consumeRule , availableCouponList , notAvailableCouponList , requestParam.getOrderAmount ());
            } , executorService).orTimeout (10 , TimeUnit.SECONDS))
            .toArray (CompletableFuture[]::new)
        );
        // 等待两个任务完成
        CompletableFuture.allOf (emptyGoodsTasks,notEmptyGoodsTasks)
            .thenRun (() -> availableCouponList.sort ((c1, c2) -> c2.getCouponAmount ().compareTo (c1.getCouponAmount ())))
            .join ();
        return QueryCouponsRespDTO.builder ()
            .availableCouponList (availableCouponList)
            .notAvailableCouponList (notAvailableCouponList)
            .build ();
    }
    
    public void calculateCouponTemplateAmount(QueryCouponsDetailRespDTO couponsDetailRespDTO,
                                              JSONObject consumeRule,
                                              List<QueryCouponsDetailRespDTO> availableCouponList,
                                              List<QueryCouponsDetailRespDTO> notAvailableCouponList,
                                              BigDecimal orderAmount){
        if (ObjectUtil.isNull (consumeRule)){
            notAvailableCouponList.add (couponsDetailRespDTO);
            return;
        }
        BigDecimal maximumDiscountAmount = consumeRule.getBigDecimal("maximumDiscountAmount");
        switch (couponsDetailRespDTO.getType ()){
            case 0:
                // 立减券
                if (orderAmount.compareTo (maximumDiscountAmount) < 0){
                    notAvailableCouponList.add (couponsDetailRespDTO);
                }else {
                    couponsDetailRespDTO.setCouponAmount (maximumDiscountAmount);
                    availableCouponList.add (couponsDetailRespDTO);
                }
                break;
            case 1:
                // 满减券
                if (orderAmount.compareTo (consumeRule.getBigDecimal ("termsOfUse")) >= 0){
                    couponsDetailRespDTO.setCouponAmount (maximumDiscountAmount);
                    availableCouponList.add (couponsDetailRespDTO);
                }else {
                    notAvailableCouponList.add (couponsDetailRespDTO);
                }
                break;
            case 2:
                // 折扣券
                if (orderAmount.compareTo (consumeRule.getBigDecimal ("termsOfUse")) >= 0){
                    BigDecimal discountRate = consumeRule.getBigDecimal ("discountRate");
                    BigDecimal multiply = orderAmount.multiply (discountRate);
                    if (multiply.compareTo (maximumDiscountAmount) >= 0 ){
                        couponsDetailRespDTO.setCouponAmount (maximumDiscountAmount);
                    }else {
                        couponsDetailRespDTO.setCouponAmount (multiply);
                    }
                    availableCouponList.add (couponsDetailRespDTO);
                }else {
                    notAvailableCouponList.add (couponsDetailRespDTO);
                }
                break;
            default:
                throw new ClientException ("无效的优惠券类型");
        }
    }
    
    @Override
    public QueryCouponsRespDTO listQueryUserCouponsBySync (QueryCouponsReqDTO requestParam) {
        // 查询缓存优惠券id
        Set<String> cacheCouponTemplateIdList = stringRedisTemplate.opsForZSet ().range (
                String.format (USER_COUPON_TEMPLATE_LIST_KEY , UserContext.getUserId ()) , 0 , -1);
        if (ObjectUtil.isEmpty (cacheCouponTemplateIdList)){
            return null;
        }
        // 根据优惠券id构建缓存优惠券key
        List<String> cacheCouponTemplateKeyList = cacheCouponTemplateIdList.stream ()
                .map (each -> StrUtil.split (each , "_").get (0))
                .map (each -> redisDistributedProperties.getPrefix() + String.format (COUPON_TEMPLATE_KEY , each))
                .toList ();
        // 使用Pipeline批量查询缓存优惠券json
        List<Object> couponTemplateList = stringRedisTemplate.executePipelined ((RedisCallback<String>) connection -> {
            cacheCouponTemplateKeyList.forEach (each -> connection.hashCommands ().hGetAll (each.getBytes ()));
            // 结束回调函数
            return null;
        });
        // 转换实体
        List<CouponTemplateQueryRespDTO> couponTemplateQueryRespDTOList = couponTemplateList.stream()
                .map(item -> BeanUtil.toBean (item,CouponTemplateQueryRespDTO.class))
                .toList ();
        // 按照商品券和店铺券分类 方便计算折扣
        Map<Boolean, List<CouponTemplateQueryRespDTO>> couponTemplateByGoods = couponTemplateQueryRespDTOList.stream ()
                .collect (Collectors.partitioningBy (coupon -> StrUtil.isEmpty (coupon.getGoods ())));
        List<CouponTemplateQueryRespDTO> emptyGoodsList = couponTemplateByGoods.get (true);
        List<CouponTemplateQueryRespDTO> hasGoodsList = couponTemplateByGoods.get (false);
        // 优惠券列表
        List<QueryCouponsDetailRespDTO> availableCouponList = new ArrayList<> ();
        List<QueryCouponsDetailRespDTO> notAvailableCouponList = new ArrayList<> ();
        // 计算店铺券折扣金额
        emptyGoodsList.forEach (each -> {
            QueryCouponsDetailRespDTO couponsDetailRespDTO = BeanUtil.toBean (each , QueryCouponsDetailRespDTO.class);
            JSONObject consume = JSON.parseObject (couponsDetailRespDTO.getConsumeRule ());
            if (ObjectUtil.isNull (consume)){
                notAvailableCouponList.add (couponsDetailRespDTO);
                return;
            }
            BigDecimal maximumDiscountAmount = consume.getBigDecimal("maximumDiscountAmount");
            switch (each.getType ()){
                case 0:
                    // 立减券
                    if (requestParam.getOrderAmount ().compareTo (maximumDiscountAmount) < 0){
                        notAvailableCouponList.add (couponsDetailRespDTO);
                    }else {
                        couponsDetailRespDTO.setCouponAmount (maximumDiscountAmount);
                        availableCouponList.add (couponsDetailRespDTO);
                    }
                    break;
                case 1:
                    // 满减券
                    if (requestParam.getOrderAmount ().compareTo (consume.getBigDecimal ("termsOfUse")) >= 0){
                        couponsDetailRespDTO.setCouponAmount (maximumDiscountAmount);
                        availableCouponList.add (couponsDetailRespDTO);
                    }else {
                        notAvailableCouponList.add (couponsDetailRespDTO);
                    }
                    break;
                case 2:
                    // 折扣券
                    if (requestParam.getOrderAmount ().compareTo (consume.getBigDecimal ("termsOfUse")) >= 0){
                        BigDecimal discountRate = consume.getBigDecimal ("discountRate");
                        BigDecimal multiplyResult = requestParam.getOrderAmount ().multiply (discountRate);
                        if (multiplyResult.compareTo (maximumDiscountAmount) >= 0 ){
                            couponsDetailRespDTO.setCouponAmount (maximumDiscountAmount);
                        }else {
                            couponsDetailRespDTO.setCouponAmount (multiplyResult);
                        }
                        availableCouponList.add (couponsDetailRespDTO);
                    }else {
                        notAvailableCouponList.add (couponsDetailRespDTO);
                    }
                    break;
                default:
                    throw new ClientException ("无效的优惠券类型");
            }
        });
        // 计算商品专属折扣金额
        Map<String, QueryCouponGoodsReqDTO> goodsMap = requestParam.getGoodsList ().stream ()
                .collect (Collectors.toMap (QueryCouponGoodsReqDTO::getGoodsNumber ,
                        // 如果遇到重复的商品编号，保留第一个出现的商品对象
                        Function.identity () ,
                        (exist , replace) -> exist));
        hasGoodsList.forEach (each -> {
            QueryCouponsDetailRespDTO couponsDetailRespDTO = BeanUtil.toBean (each , QueryCouponsDetailRespDTO.class);
            JSONObject consume = JSON.parseObject (couponsDetailRespDTO.getConsumeRule ());
            BigDecimal maximumDiscountAmount = consume.getBigDecimal("maximumDiscountAmount");
            // 从请求参数里的商品编号校验是否有该商品的优惠券可用
            QueryCouponGoodsReqDTO couponGoodsReqDTO = goodsMap.get (each.getGoods ());
            if (ObjectUtil.isNull (couponGoodsReqDTO)) {
                notAvailableCouponList.add (couponsDetailRespDTO);
            }
            switch (each.getType ()){
                case 0:
                    // 立减券
                    if (requestParam.getOrderAmount ().compareTo (maximumDiscountAmount) < 0){
                        notAvailableCouponList.add (couponsDetailRespDTO);
                    }else {
                        couponsDetailRespDTO.setCouponAmount (maximumDiscountAmount);
                        availableCouponList.add (couponsDetailRespDTO);
                    }
                    break;
                case 1:
                    // 满减券
                    if (couponGoodsReqDTO.getGoodsAmount ().compareTo (consume.getBigDecimal ("termsOfUse")) >= 0){
                        couponsDetailRespDTO.setCouponAmount (maximumDiscountAmount);
                        availableCouponList.add (couponsDetailRespDTO);
                    }else {
                        notAvailableCouponList.add (couponsDetailRespDTO);
                    }
                    break;
                case 2:
                    // 折扣券
                    if (couponGoodsReqDTO.getGoodsAmount ().compareTo (consume.getBigDecimal ("termsOfUse")) >= 0){
                        BigDecimal discountRate = consume.getBigDecimal ("discountRate");
                        BigDecimal multiply = couponGoodsReqDTO.getGoodsAmount ().multiply (discountRate);
                        if (multiply.compareTo (maximumDiscountAmount) >= 0 ){
                            couponsDetailRespDTO.setCouponAmount (maximumDiscountAmount);
                        }else {
                            couponsDetailRespDTO.setCouponAmount (multiply);
                        }
                        availableCouponList.add (couponsDetailRespDTO);
                    }else {
                        notAvailableCouponList.add (couponsDetailRespDTO);
                    }
                    break;
                default:
                    throw new ClientException ("无效的优惠券类型");
            }
        });
        // 按照从大到小排序
        availableCouponList.sort ((c1,c2) ->
                c2.getCouponAmount ().compareTo (c1.getCouponAmount ()));
        return QueryCouponsRespDTO.builder ()
                .availableCouponList (availableCouponList)
                .notAvailableCouponList (notAvailableCouponList)
                .build ();
    }
}
