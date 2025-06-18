package com.fengxin.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author FENGXIN
 * @date 2024/10/20
 * @project feng-coupon
 * @description 防止重复提交幂等
 **/
@Target (value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface DuplicateSubmit {
    String message() default "您的操作太快，请稍后再试";
}
