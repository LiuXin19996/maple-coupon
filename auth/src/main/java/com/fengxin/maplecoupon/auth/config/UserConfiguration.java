package com.fengxin.maplecoupon.auth.config;


import com.fengxin.maplecoupon.auth.common.context.UserContext;
import com.fengxin.maplecoupon.auth.common.context.UserInfoDTO;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 用户相关配置
 **/
@Configuration
public class UserConfiguration implements WebMvcConfigurer {
    /**
     * 用户信息传输拦截器
     */
    @Bean
    public UserTransmitInterceptor userTransmitInterceptor() {
        return new UserTransmitInterceptor();
    }
    
    /**
     * 添加用户信息传递过滤器至相关路径拦截
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userTransmitInterceptor())
                .order (0)
                .excludePathPatterns("/**");
    }
    
    /**
     * 用户信息传输拦截器
     */
    static class UserTransmitInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler) throws Exception {
            String userId;
            if (request != null) {
                userId = Optional.ofNullable (request.getParameter ("userId"))
                        .orElseThrow (() -> new RuntimeException ("用户id缺失"));
                String userName = Optional.ofNullable (request.getParameter ("username"))
                        .orElseThrow (() -> new RuntimeException ("用户名缺失"));
                String shopNumber = Optional.ofNullable (request.getParameter ("shopNumber"))
                        .orElse("");
                UserInfoDTO userInfoDTO = new UserInfoDTO (userId, userName, shopNumber);
                UserContext.setUser(userInfoDTO);
            }
            return true;
        }
        
        @Override
        public void afterCompletion(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler, Exception exception) throws Exception {
            UserContext.removeUser();
        }
    }

}
