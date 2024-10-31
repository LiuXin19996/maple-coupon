package com.fengxin.maplecoupon.engine.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.fengxin.exception.ClientException;
import com.fengxin.maplecoupon.engine.common.enums.CouponRemindTypeEnum;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateRemindQueryRespDTO;

import javax.swing.text.SimpleAttributeSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    
    private static final int REMIND_TYPE_COUNT = CouponRemindTypeEnum.values ().length;
    
    public static Long calculateRemindTime(Integer remindTime, Integer type){
        if (remindTime > TIME_INTERVAL * NEXT_TYPE_BITS){
            throw new ClientException ("预约提醒的时间不能早于开票前" + TIME_INTERVAL * NEXT_TYPE_BITS + "分钟");
        }
        // 把相应类型的通知的时间放进相应的位里
        return 1L << (type * NEXT_TYPE_BITS + remindTime / TIME_INTERVAL - 1);
    }
    
    public static void fillRemindInformation(CouponTemplateRemindQueryRespDTO remindQueryRespDTO, Long information){
        List<Date> remindTimeList = new ArrayList<> ();
        List<String> remindType = new ArrayList<> ();
        Date validStartTime = remindQueryRespDTO.getValidStartTime ();
        // 遍历每一个类型的每一个时间点 对information运算
        for (int i = NEXT_TYPE_BITS - 1; i >= 0; i--){
            for (int j = 0; j < REMIND_TYPE_COUNT; j++){
                // 填充 右移到最后一位 & 1L == 1说明有时间点提醒
                if ((information >> (j * NEXT_TYPE_BITS + i) & 1L) == 1){
                    // 说明该时间节点有预约提醒
                    DateTime time = DateUtil.offsetMinute (validStartTime , -(TIME_INTERVAL * (i + 1)));
                    remindTimeList.add (time);
                    remindType.add (CouponRemindTypeEnum.getDescribeByType (j));
                }
            }
        }
        remindQueryRespDTO.setRemindTime (remindTimeList);
        remindQueryRespDTO.setRemindType (remindType);
    }
}
