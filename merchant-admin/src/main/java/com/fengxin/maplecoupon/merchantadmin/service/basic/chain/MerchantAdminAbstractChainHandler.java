package com.fengxin.maplecoupon.merchantadmin.service.basic.chain;

import org.springframework.core.Ordered;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 抽象商家后管业务责任链组件
 **/
public interface MerchantAdminAbstractChainHandler<T> extends Ordered {
    /**
     * 执行责任链逻辑
     *
     * @param requestParam 责任链执行入参
     */
    void handler(T requestParam);
    
    /**
     * @return 责任链组件标识
     */
    String mark();
    
}
