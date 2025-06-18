package com.fengxin.maplecoupon.engine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project Default (Template) Project
 * @description 引擎服务 | 负责券锁定、核销、查看等功能
 **/
@SpringBootApplication
@MapperScan("com.fengxin.maplecoupon.engine.dao.mapper")
public class EngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(EngineApplication.class, args);
    }
}