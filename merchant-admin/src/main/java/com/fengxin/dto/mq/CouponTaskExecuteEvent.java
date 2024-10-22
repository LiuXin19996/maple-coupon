package com.fengxin.dto.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 消息发送事件实体
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponTaskExecuteEvent {
    /**
     * 优惠券任务 ID
     */
    private Long couponTaskId;
}
