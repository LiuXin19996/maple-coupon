package com.fengxin.maplecoupon.distribution.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 数据库持久层配置
 **/
@Configuration
public class DataBaseConfiguration {
    
    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor (new PaginationInnerInterceptor (DbType.MYSQL));
        return interceptor;
    }
    
    /**
     * 元数据自动填充
     */
    @Bean
    public MyMetaObjectHandler myMetaObjectHandler() {
        return new MyMetaObjectHandler();
    }
    
    
    static class MyMetaObjectHandler implements MetaObjectHandler {
        
        @Override
        public void insertFill(MetaObject metaObject) {
            strictInsertFill(metaObject, "createTime", Date::new, Date.class);
            strictInsertFill(metaObject, "updateTime", Date::new, Date.class);
            strictInsertFill(metaObject, "delFlag", () -> 0, Integer.class);
        }
        
        @Override
        public void updateFill(MetaObject metaObject) {
            strictInsertFill(metaObject, "updateTime", Date::new, Date.class);
        }
    }
    
}
