package com.fengxin.maplecoupon.auth.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FENGXIN
 */

@Getter
@RequiredArgsConstructor
public enum CouponRemindTypeEnum {

    /**
     * App 通知
     */
    APP(0, "App通知"),

    /**
     * 邮件提醒
     */
    EMAIL(1, "邮件提醒");

    private final int type;
    private final String describe;

    public static CouponRemindTypeEnum getByType(Integer type) {
        for (CouponRemindTypeEnum remindEnum : values()) {
            if (remindEnum.getType() == type) {
                return remindEnum;
            }
        }
        return null;
    }

    public static String getDescribeByType(Integer type) {
        for (CouponRemindTypeEnum remindEnum : values()) {
            if (remindEnum.getType() == type) {
                return remindEnum.getDescribe();
            }
        }
        return null;
    }
}
