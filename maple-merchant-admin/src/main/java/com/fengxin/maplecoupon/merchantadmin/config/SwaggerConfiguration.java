package com.fengxin.maplecoupon.merchantadmin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description API借口文档配置
 **/
@Slf4j
@Configuration
public class SwaggerConfiguration implements ApplicationRunner {
    
    @Value("${server.port:8080}")
    private String serverPort;
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    /**
     * 自定义 openAPI 个性化信息
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI ()
                .info(new Info ()
                        .title("MapleCoupon-商家后台管理系统")
                        .description("创建优惠券、店家查看以及管理优惠券、创建优惠券发放批次等")
                        .version("v1.0.0")
                        // 设置 OpenAPI 文档的联系信息，包括联系人姓名为"xin.feng"，邮箱为"fx20031215@163.com"
                        .contact(new Contact ().name("xin.feng").email("fx20031215@163.com"))
                        // 设置 OpenAPI 文档的许可证信息，包括许可证名称和许可证URL
                        .license(new License ().name("山东流年网络科技有限公司").url("https://gitcode.net/nageoffer/onecoupon/-/blob/main/LICENSE"))
                );
    }
    
    
    @Override
    public void run (ApplicationArguments args) throws Exception {
        log.info("API Document: http://127.0.0.1:{}{}/doc.html", serverPort, contextPath);
    }
}
