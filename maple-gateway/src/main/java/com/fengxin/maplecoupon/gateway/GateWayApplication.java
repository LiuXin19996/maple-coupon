package com.fengxin.maplecoupon.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project feng-coupon
 * @description 网关服务｜负责请求路由转发、请求日志打印、接口限流等功能
 **/
@SpringBootApplication
public class GateWayApplication {
    public static void main (String[] args) {
        SpringApplication.run(GateWayApplication.class, args);
    }
}
