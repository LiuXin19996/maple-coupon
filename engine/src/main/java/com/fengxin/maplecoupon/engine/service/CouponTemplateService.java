package com.fengxin.maplecoupon.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.fengxin.maplecoupon.engine.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateQueryReqDTO;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateRemindTimeReqDTO;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateQueryRespDTO;


/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券模板业务接口
 **/
public interface CouponTemplateService extends IService<CouponTemplateDO> {
    
    /**
     * 按 ID 查找优惠券模板
     *
     * @param requestParam 请求参数
     * @return {@code CouponTemplateQueryRespDTO }
     */
    CouponTemplateQueryRespDTO findCouponTemplateById (CouponTemplateQueryReqDTO requestParam);
    
    /**
     * 创建优惠券提醒
     *
     * @param requestParam 请求参数
     */
    void createCouponRemind (CouponTemplateRemindTimeReqDTO requestParam);
}
