package com.fengxin.maplecoupon.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 身份验证应用程序
 *
 * @author fengxin
 * @date 2024-12-26
 */
@SpringBootApplication
@MapperScan("com.fengxin.maplecoupon.auth.dao.mapper")
public class AuthApplication {
    
    public static void main (String[] args) {
        SpringApplication.run (AuthApplication.class , args);
    }
    
}
