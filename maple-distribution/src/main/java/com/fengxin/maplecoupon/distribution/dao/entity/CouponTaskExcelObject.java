package com.fengxin.maplecoupon.distribution.dao.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author FENGXIN
 */
@Data
public class CouponTaskExcelObject {

    @ExcelProperty("用户ID")
    private String userId;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String mail;
}
