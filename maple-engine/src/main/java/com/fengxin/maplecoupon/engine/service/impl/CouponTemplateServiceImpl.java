package com.fengxin.maplecoupon.engine.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengxin.exception.ServiceException;
import com.fengxin.maplecoupon.engine.common.enums.CouponTemplateStatusEnum;
import com.fengxin.maplecoupon.engine.dao.mapper.CouponTemplateMapper;
import com.fengxin.maplecoupon.engine.dao.mapper.UserCouponMapper;
import com.fengxin.maplecoupon.engine.dao.sharding.DBShardingUtil;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateQueryReqDTO;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.maplecoupon.engine.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.engine.service.CouponTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.fengxin.maplecoupon.engine.common.constant.EngineRedisConstant.*;

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
    private final RedissonClient redissonClient;
    private final RBloomFilter<String> couponTemplateQueryBloomFilter;
    private final UserCouponMapper userCouponMapper;
    
    @Override
    public CouponTemplateQueryRespDTO findCouponTemplateById (CouponTemplateQueryReqDTO requestParam) {
        // Redis缓存 解决缓存击穿
        // 缓存key
        String cacheCouponTemplateKey = String.format (COUPON_TEMPLATE_KEY, requestParam.getCouponTemplateId());
        String lockCouponTemplateKey = String.format (LOCK_COUPON_TEMPLATE_KEY, requestParam.getCouponTemplateId());
        String emptyCouponTemplateKey = String.format (EMPTY_COUPON_TEMPLATE_KEY,requestParam.getCouponTemplateId ());
        // 获取缓存中的所有优惠券模板数据
        Map<Object, Object> cacheCouponTemplateMap = stringRedisTemplate.opsForHash ().entries (cacheCouponTemplateKey);
        if (MapUtil.isEmpty (cacheCouponTemplateMap)) {
            // 先查询布隆过滤器是否存在
            if (!couponTemplateQueryBloomFilter.contains (requestParam.getCouponTemplateId())) {
                throw new ServiceException ("优惠券已过期");
            }
            // 如果布隆过滤器存在值 查询是否有空值 防止数据库删除了优惠券模板但布隆过滤器还存在
            if (stringRedisTemplate.hasKey (emptyCouponTemplateKey)) {
                throw new ServiceException ("优惠券已过期");
            }
            // 分布式🔒 这里用户需要真实数据 必须保证数据正确 所以采用lock()
            RLock lock = redissonClient.getLock (lockCouponTemplateKey);
            lock.lock ();
            try {
                // 双重判定🔒
                cacheCouponTemplateMap = stringRedisTemplate.opsForHash ().entries (cacheCouponTemplateKey);
                if (MapUtil.isEmpty (cacheCouponTemplateMap)) {
                    // 先查询布隆过滤器是否存在 如果是第一个线程 查 如果是之后的线程 直接判断缓存
                    if (!couponTemplateQueryBloomFilter.contains (requestParam.getCouponTemplateId())) {
                        throw new ServiceException ("优惠券已过期");
                    }
                    // 如果布隆过滤器存在值 查询是否有空值 防止数据库删除了优惠券模板而布隆过滤器还存在
                    if (stringRedisTemplate.hasKey (emptyCouponTemplateKey)) {
                        throw new ServiceException ("优惠券已过期");
                    }
                    // 查询数据库数据
                    LambdaQueryWrapper<CouponTemplateDO> queryWrapper = new LambdaQueryWrapper<CouponTemplateDO>()
                            .eq (CouponTemplateDO::getId,Long.valueOf (requestParam.getCouponTemplateId ()))
                            .eq (CouponTemplateDO::getStatus,CouponTemplateStatusEnum.ACTIVE.getValue ())
                            .eq (CouponTemplateDO::getDelFlag, 0)
                            .eq (CouponTemplateDO::getShopNumber,Long.valueOf (requestParam.getShopNumber ()));
                    CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne (queryWrapper);
                    if (ObjectUtil.isEmpty (couponTemplateDO)) {
                        // 设置缓存空值
                        stringRedisTemplate.opsForValue ().set (emptyCouponTemplateKey,"",30, TimeUnit.MINUTES);
                        throw new ServiceException ("优惠券已过期");
                    }
                    
                    // 放入redis缓存
                    CouponTemplateQueryRespDTO couponTemplateQueryRespDTO = BeanUtil.toBean (couponTemplateDO , CouponTemplateQueryRespDTO.class);
                    Map<String, Object> couponTemplateQueryRespMap = BeanUtil.beanToMap (couponTemplateQueryRespDTO , false , true);
                    Map<String, String> actualCouponTemplateQueryRespMap = couponTemplateQueryRespMap.entrySet ().stream ().collect (
                            Collectors.toMap (Map.Entry::getKey , entry -> entry.getValue () != null ? entry.getValue ().toString () : "")
                    );
                    
                    // 通过 LUA 脚本执行设置 Hash 数据以及设置过期时间
                    String luaScript = "redis.call('HMSET', KEYS[1], unpack(ARGV, 1, #ARGV - 1)) " +
                            "redis.call('EXPIREAT', KEYS[1], ARGV[#ARGV])";
                    // 接口规定的List 否则可以使用单独的key串
                    List<String> keys = Collections.singletonList(cacheCouponTemplateKey);
                    // 设置参数列表方便存入redis缓存
                    List<String> args = new ArrayList<> (actualCouponTemplateQueryRespMap.size() * 2 + 1);
                    actualCouponTemplateQueryRespMap.forEach((key, value) -> {
                        args.add(key);
                        args.add(value);
                    });
                    
                    // 优惠券活动过期时间转换为秒级别的 Unix 时间戳
                    args.add(String.valueOf(couponTemplateDO.getValidEndTime().getTime() / 1000));
                    
                    // 执行 LUA 脚本
                    stringRedisTemplate.execute(
                            new DefaultRedisScript<> (luaScript, Long.class),
                            keys,
                            args.toArray()
                    );
                    cacheCouponTemplateMap = couponTemplateQueryRespMap.entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                }
            }finally {
                lock.unlock ();
            }
        }
        return BeanUtil.mapToBean (cacheCouponTemplateMap,CouponTemplateQueryRespDTO.class,false, CopyOptions.create ());
    }
    
    @Override
    public List<CouponTemplateDO> listCouponTemplateByIdAndShopNumber (List<Long> couponTemplateIdList , List<Long> shopNumberList) {
        // 获取每个分库下的店铺编号
        Map<Integer, List<Long>> couponDatabase = splitCouponDatabase (shopNumberList);
        List<CouponTemplateDO> result = new ArrayList<> ();
        // 对每个数据库查询
        for (Map.Entry<Integer, List<Long>> entry : couponDatabase.entrySet ()) {
            List<Long> shopNumberByDatabase = entry.getValue ();
            List<CouponTemplateDO> couponTemplateDOList = queryListByDatabase (couponTemplateIdList , shopNumberByDatabase);
            result.addAll (couponTemplateDOList);
        }
        return result;
    }
    
    /**
     * 拆分优惠券数据库
     *
     * @param shopNumberList 店铺编号一览
     * @return {@code Map<Integer,List<Long>> 数据库编号 shopNumbers}
     */
    public Map<Integer,List<Long>> splitCouponDatabase(List<Long> shopNumberList){
        Map<Integer,List<Long>> result = new HashMap<> ();
        for (Long shopNumber : shopNumberList) {
            int database = DBShardingUtil.doCouponSharding (shopNumber);
            // 添加shopNumber到对应数据库索引的列表里
            result.computeIfAbsent (database,n -> new ArrayList<> ())
                    .add (shopNumber);
        }
        return result;
    }
    
    /**
     * 按数据库查询列表
     *
     * @param couponTemplateIdList 优惠券模板 ID 列表
     * @param shopNumberList       店铺编号一览
     * @return {@code List<CouponTemplateDO> }
     */
    public List<CouponTemplateDO> queryListByDatabase(List<Long> couponTemplateIdList , List<Long> shopNumberList){
        LambdaQueryWrapper<CouponTemplateDO> queryWrapper = new LambdaQueryWrapper<CouponTemplateDO> ()
                .in (CouponTemplateDO::getId,couponTemplateIdList)
                .in (CouponTemplateDO::getShopNumber,shopNumberList);
        return couponTemplateMapper.selectList (queryWrapper);
    }
}
