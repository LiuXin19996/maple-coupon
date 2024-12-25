package com.fengxin.maplecoupon.engine.dto.resp;

import lombok.Data;

/**
 * @author FENGXIN
 * @date 2024/9/26
 * @project feng-shortlink
 * @description 用户真是信息响应
 **/
@Data
public class UserActualRespDTO {
    
    /**
     * 用户的唯一标识
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
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
