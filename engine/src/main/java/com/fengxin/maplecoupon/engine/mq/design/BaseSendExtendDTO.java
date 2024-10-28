package com.fengxin.maplecoupon.engine.mq.design;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 消息发送基础信息实体
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseSendExtendDTO {
    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 主题
     */
    private String topic;

    /**
     * 标签
     */
    private String tag;

    /**
     * 业务标识
     */
    private String keys;

    /**
     * 发送消息超时时间
     */
    private Long sentTimeout;

    /**
     * 具体延迟时间
     */
    private Long delayTime;
}
