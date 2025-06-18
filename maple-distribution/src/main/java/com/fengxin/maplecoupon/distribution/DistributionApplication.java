package com.fengxin.maplecoupon.distribution;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project Default (Template) Project
 * @description 分发服务 | 负责按批次分发用户优惠券，可提供应用弹框推送、站内信或短信通知等
 **/
@SpringBootApplication
@MapperScan("com.fengxin.maplecoupon.distribution.dao.mapper")
public class DistributionApplication {
    public static void main (String[] args) {
        SpringApplication.run(DistributionApplication.class, args);
    }
}