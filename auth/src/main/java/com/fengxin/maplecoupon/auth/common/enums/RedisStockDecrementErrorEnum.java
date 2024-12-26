package com.fengxin.maplecoupon.auth.common.enums;

import com.fengxin.exception.ServiceException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/28
 * @project feng-coupon
 * @description Redis 扣减优惠券库存错误枚举
 **/
@Getter
@RequiredArgsConstructor
public enum RedisStockDecrementErrorEnum {
    /**
     * 成功
     */
    SUCCESS(0, "成功"),
    
    /**
     * 库存不足
     */
    STOCK_INSUFFICIENT(1, "优惠券已被领取完啦"),
    
    /**
     * 用户已经达到领取上限
     */
    LIMIT_REACHED(2, "用户已经达到领取上限");
    
    private final long code;
    
    private final String message;
    
    /**
     * 根据 code 找到对应的枚举实例判断是否成功标识
     *
     * @param code 要查找的编码
     * @return 是否成功标识
     */
    public static boolean isFail (long code){
        for (RedisStockDecrementErrorEnum value : values ()) {
            if (value.code == code){
                return value != SUCCESS;
            }
        }
        return false;
    }
    
    /**
     * 根据 type 找到对应的枚举实例
     *
     * @param code 要查找的编码
     * @return 对应的枚举实例
     */
    public static String fromType(long code){
        for (RedisStockDecrementErrorEnum value : values ()) {
            if (value.code == code){
                return value.getMessage ();
            }
        }
        throw new ServiceException ("非法 code: " + code);
    }
    
}
