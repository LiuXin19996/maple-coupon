package com.fengxin.maplecoupon.engine.dto.req;

import lombok.Data;

/**
 * @author FENGXIN
 * @date 2024/9/27
 * @project feng-shortlink
 * @description 用户注册请求参数实体
 **/
@Data
public class UserRegisterReqDTO {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户密码
     */
    private String password;
    
    /**
     * 用户店铺名
     */
    private String shopName;
    
    /**
     * 用户真实姓名
     */
    private String realName;
    
    /**
     * 用户电话号码
     */
    private String phone;
    
    /**
     * 用户邮箱地址
     */
    private String mail;
}
