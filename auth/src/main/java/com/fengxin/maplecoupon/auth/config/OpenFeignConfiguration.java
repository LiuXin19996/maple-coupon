package com.fengxin.maplecoupon.auth.config;

import com.fengxin.maplecoupon.auth.common.context.UserContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * @author FENGXIN
 * @date 2024/10/13
 * @project feng-shortlink
 * @description openFeign 微服务调用传递用户信息配置
 **/
@Configuration
public class OpenFeignConfiguration {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header ("username", UserContext.getUsername ());
            template.header ("userId", String.valueOf (UserContext.getUserId ()));
            template.header ("shopNumber", String.valueOf (UserContext.getShopNumber ()));
        };
    }
}
