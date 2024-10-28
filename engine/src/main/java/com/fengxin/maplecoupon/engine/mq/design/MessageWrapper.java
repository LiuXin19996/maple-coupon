package com.fengxin.maplecoupon.engine.mq.design;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 消息体包装器
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force=true)
@RequiredArgsConstructor
public final class MessageWrapper<T> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息发送 Keys
     */
    @NonNull
    private String keys;

    /**
     * 消息体
     */
    @NonNull
    private T message;

    /**
     * 消息发送时间
     */
    private Long timestamp = System.currentTimeMillis();
    
}
