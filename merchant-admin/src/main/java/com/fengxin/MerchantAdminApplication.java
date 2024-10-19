package com.fengxin;

import com.mzt.logapi.starter.annotation.EnableLogRecord;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project Default (Template) Project
 * @description 商家后台管理｜创建优惠券、店家查看以及管理优惠券、创建优惠券发放批次等
 **/
@SpringBootApplication
@MapperScan("com.fengxin.dao.mapper")
// 日志
@EnableLogRecord (tenant = "MerchantAdmin")
public class MerchantAdminApplication {
    public static void main (String[] args) {
        SpringApplication.run(MerchantAdminApplication.class, args);
    }
}