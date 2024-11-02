package com.fengxin.maplecoupon.settlement.service;

import com.fengxin.maplecoupon.settlement.dto.req.QueryCouponsReqDTO;
import com.fengxin.maplecoupon.settlement.dto.resp.QueryCouponsRespDTO;
import org.springframework.stereotype.Service;

/**
 * 优惠券查询服务
 *
 * @author fengxin
 * @date 2024-11-02
 */
public interface CouponQueryService {
    /**
     * 列表查询用户优惠券
     *
     * @param requestParam 请求参数
     * @return {@code QueryCouponsRespDTO }
     */
    QueryCouponsRespDTO listQueryUserCoupons (QueryCouponsReqDTO requestParam);
    
    /**
     * 列表 按同步查询用户优惠券
     *
     * @param requestParam 请求参数
     * @return {@code QueryCouponsRespDTO }
     */
    QueryCouponsRespDTO listQueryUserCouponsBySync (QueryCouponsReqDTO requestParam);
}
