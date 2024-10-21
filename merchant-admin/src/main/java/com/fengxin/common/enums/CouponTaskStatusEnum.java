package com.fengxin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/21
 * @project feng-coupon
 * @description 优惠券推送任务执行状态
 **/
@RequiredArgsConstructor
public enum CouponTaskStatusEnum {
    /**
     * 待执行
     */
    PENDING(0),
    
    /**
     * 执行中
     */
    IN_PROGRESS(1),
    
    /**
     * 执行失败
     */
    FAILED(2),
    
    /**
     * 执行成功
     */
    SUCCESS(3),
    
    /**
     * 取消
     */
    CANAL(4);
    
    @Getter
    private final int status;
    
}
