package com.fengxin.maplecoupon.merchantadmin.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author FENGXIN
 * @date 2024/10/19
 * @project feng-coupon
 * @description
 **/
public final class MerchantAdminUtils {
    /**
     * 转换日期格式
     */
    public static Date formatDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
