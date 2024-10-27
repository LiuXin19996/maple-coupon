package com.fengxin.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/27
 * @project feng-coupon
 * @description 消费状态枚举
 **/
@RequiredArgsConstructor
public enum MQConsumeStatusEnum {
    /**
     * 消费中
     */
    CONSUMING ("0"),
    
    /**
     * 消费完成
     */
    CONSUMED ("1");
    
    @Getter
    private final String code;
    
    
    /**
     * 返回消费中的状态 使消费者重试消费
     *
     * @param consumeStatus 消费状态
     * @return boolean 消费失败
     */
    public static boolean isError(String consumeStatus){
        return ObjectUtil.equals (consumeStatus, CONSUMING);
    }
}
