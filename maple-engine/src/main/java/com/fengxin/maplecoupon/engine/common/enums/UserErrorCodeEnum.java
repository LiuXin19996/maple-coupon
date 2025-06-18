package com.fengxin.maplecoupon.engine.common.enums;

/**
 * @author FENGXIN
 * @date 2024/9/26
 * @project feng-shortlink
 * @description 用户异常码规定
 **/
public enum UserErrorCodeEnum {
    // 客户端异常码
    USER_NAME_TOKEN_ERROR("A000200","用户验证失败"),
    // 服务端异常码
    USER_NAME_NULL("B000200","用户名不存在"),
    USER_NAME_EXISTS("B000201","用户名已存在"),
    USER_NULL("B000202","用户不存在"),
    USER_EXISTS ("B000203","用户已存在"),
    USER_SAVE_ERROR("B000204","用户新增失败"),
    USER_LOGIN_ERROR ("B000205" , "用户已登录"),
    USER_LOGOUT_ERROR ("B000206" , "用户退出登录失败");
    
    private final String code;
    private final String message;
    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String code () {
        return code;
    }
    
    public String message () {
        return message;
    }
}
