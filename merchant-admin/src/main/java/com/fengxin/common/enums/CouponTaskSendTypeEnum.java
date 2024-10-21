package com.fengxin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/21
 * @project feng-coupon
 * @description 分发优惠券类型
 **/
@RequiredArgsConstructor
public enum CouponTaskSendTypeEnum {
    /**
     * 立即发送
     */
    IMMEDIATE(0),
    
    /**
     * 定时发送
     */
    SCHEDULED(1);
    
    @Getter
    private final int type;

}
