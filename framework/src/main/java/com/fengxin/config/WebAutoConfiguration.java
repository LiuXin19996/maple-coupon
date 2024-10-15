package com.fengxin.config;

import com.fengxin.web.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project feng-coupon
 * @description web组件自动装配
 **/
public class WebAutoConfiguration {
    /**
     * 构建全局异常拦截器组件 Bean
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

}
