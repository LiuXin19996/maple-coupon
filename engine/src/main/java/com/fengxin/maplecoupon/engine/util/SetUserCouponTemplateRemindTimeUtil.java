package com.fengxin.maplecoupon.engine.util;

import com.fengxin.exception.ClientException;

/**
 * @author FENGXIN
 * @date 2024/10/30
 * @project feng-coupon
 * @description 优惠券提醒时间bitmap
 **/
public class SetUserCouponTemplateRemindTimeUtil {
    /**
     * 下一个类型的位移量，每个类型占用12个bit位，共计60分钟
     */
    private static final int NEXT_TYPE_BITS = 12;

    /**
     * 5分钟为一个间隔
     */
    private static final int TIME_INTERVAL = 5;
    
    public static Long calculateRemindTime(Integer remindTime, Integer type){
        if (remindTime > TIME_INTERVAL * NEXT_TYPE_BITS){
            throw new ClientException ("预约提醒的时间不能早于开票前" + TIME_INTERVAL * NEXT_TYPE_BITS + "分钟");
        }
        // 把相应类型的通知的时间放进相应的位里
        return 1L << (type * NEXT_TYPE_BITS + Math.max (0,remindTime / TIME_INTERVAL - 1));
    }
}
