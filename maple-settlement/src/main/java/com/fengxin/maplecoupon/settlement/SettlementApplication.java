package com.fengxin.maplecoupon.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project Default (Template) Project
 * @description 结算服务 | 负责用户下单时订单金额计算功能 因和订单相关联，该服务流量较大
 **/
@SpringBootApplication
public class SettlementApplication {
    public static void main (String[] args) {
        SpringApplication.run(SettlementApplication.class, args);
    }
}