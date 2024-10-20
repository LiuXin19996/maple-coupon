package com.fengxin.config;

import com.fengxin.idempotent.DuplicateSubmitAspect;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author FENGXIN
 * @date 2024/10/20
 * @project feng-coupon
 * @description 幂等自动配置类
 **/
@Configuration
public class IdempotentConfiguration {
    /**
     * 防止用户重复提交表单信息切面控制器
     *
     * @param redissonClient Redisson 客户端
     * @return {@code DuplicateSubmitAspect }
     */
    @Bean
    public DuplicateSubmitAspect duplicateSubmitAspect(RedissonClient redissonClient) {
        return new DuplicateSubmitAspect (redissonClient);
    }
}
