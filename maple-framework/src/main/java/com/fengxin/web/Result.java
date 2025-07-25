package com.fengxin.web;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project feng-coupon
 * @description 定义全局返回对象｜方便接口参数返回约束，避免不同的参数定义混淆前端接收
 **/
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 5679018624309023727L;
    
    /**
     * 正确返回码
     */
    public static final String SUCCESS_CODE = "200";
    
    /**
     * 返回码
     */
    private String code;
    
    /**
     * 返回消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    public boolean isSuccess() {
        return SUCCESS_CODE.equals(code);
    }
}

