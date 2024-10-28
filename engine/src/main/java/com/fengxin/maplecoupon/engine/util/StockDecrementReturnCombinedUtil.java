package com.fengxin.maplecoupon.engine.util;

/**
 * 缓存兑换优惠券处理返回结果工具
 *
 * @author FENGXIN
 * @date 2024/10/28
 */
public class StockDecrementReturnCombinedUtil {
    /**
     * 2^14 > 9999, 所以用 14 位来表示第二个字段
     */
    private static final int SECOND_FIELD_BITS = 14;
    
    /**
     * 从组合的 int 中提取第一个字段（0、1或2）
     */
    public static long extractFirstField(long combined) {
        // 0b11 即二进制的 11，用于限制结果为 2 位
        return (combined >> SECOND_FIELD_BITS) & 0b11;
    }
    
    /**
     * 从组合的 int 中提取第二个字段（0 到 9999 之间的数字）
     */
    public static long extractSecondField(long combined) {
        return combined & ((1 << SECOND_FIELD_BITS) - 1);
    }
    
}
