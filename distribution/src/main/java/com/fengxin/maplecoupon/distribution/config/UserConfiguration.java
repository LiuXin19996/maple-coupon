package com.fengxin.maplecoupon.distribution.config;

import com.fengxin.maplecoupon.distribution.common.context.UserTransmitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 用户相关配置
 **/
@Configuration
@RequiredArgsConstructor
public class UserConfiguration implements WebMvcConfigurer {
    private final UserTransmitInterceptor userTransmitInterceptor;
    
    /**
     * 添加用户信息传递过滤器至相关路径拦截
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userTransmitInterceptor)
                .addPathPatterns("/**");
    }
    
}
