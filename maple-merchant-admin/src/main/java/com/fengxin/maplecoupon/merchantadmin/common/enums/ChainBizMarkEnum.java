package com.fengxin.maplecoupon.merchantadmin.common.enums;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 定义业务责任链类型枚举
 **/
public enum ChainBizMarkEnum {
    /**
     * 创建优惠券模板验证参数是否正确责任链流程
     */
    MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY;
    
    @Override
    public String toString() {
        return this.name();
    }

}
