package com.fengxin.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author FENGXIN
 * @date 2024/10/27
 * @project feng-coupon
 * @description mq消费者防重消费注解
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DuplicateMQConsume {
    /**
     * 防重令牌前缀
     */
    String keyPrefix() default "";
    
    /**
     * SpEL表达式生成的唯一key
     */
    String key();
    
    /**
     * 设置去重key的过期时间
     */
    long timeout() default 3600L;
}
