package com.fengxin.maplecoupon.engine.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author FENGXIN
 * @date 2024/10/24
 * @project feng-coupon
 * @description 优惠券模板布隆过滤器
 **/
@Configuration
public class RBloomFilterConfiguration {
    /**
     * 优惠券查询缓存穿透布隆过滤器
     */
    @Bean
    public RBloomFilter<String> couponTemplateQueryBloomFilter(RedissonClient redissonClient,@Value("${framework.cache.redis.prefix:}") String cachePrefix) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(cachePrefix + "-couponTemplateQueryBloomFilter");
        bloomFilter.tryInit(640L, 0.001);
        return bloomFilter;
    }
    
    /**
     * 防止取消提醒缓存穿透的布隆过滤器
     */
    @Bean
    public RBloomFilter<String> cancelRemindBloomFilter(RedissonClient redissonClient, @Value("${framework.cache.redis.prefix:}") String cachePrefix) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(cachePrefix + "-cancelRemindBloomFilter");
        bloomFilter.tryInit(640L, 0.001);
        return bloomFilter;
    }
    
    /**
     * 防止用户注册查询数据库的布隆过滤器
     */
    @Bean
    public RBloomFilter<String> userRegisterCachePenetrationBloomFilter(RedissonClient redissonClient, @Value("${framework.cache.redis.prefix:}") String cachePrefix) {
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter(cachePrefix + "-userRegisterCachePenetrationBloomFilter");
        cachePenetrationBloomFilter.tryInit(100000000, 0.001);
        return cachePenetrationBloomFilter;
    }
}
