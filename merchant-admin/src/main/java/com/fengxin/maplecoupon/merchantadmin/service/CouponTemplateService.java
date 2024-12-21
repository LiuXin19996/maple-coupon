package com.fengxin.maplecoupon.merchantadmin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fengxin.maplecoupon.merchantadmin.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTemplateNumberReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTemplatePageQueryReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTemplateSaveReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.req.TerminateCouponTemplateReqDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.resp.CouponTemplatePageQueryRespDTO;
import com.fengxin.maplecoupon.merchantadmin.dto.resp.CouponTemplateQueryRespDTO;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券模板业务接口
 **/
public interface CouponTemplateService extends IService<CouponTemplateDO> {
    
    /**
     * 创建优惠券模板
     *
     * @param requestParam 请求参数
     */
    void createCouponTemplate (CouponTemplateSaveReqDTO requestParam);
    
    /**
     * 按 ID 查找优惠券模板
     *
     * @param couponTemplateId 优惠券模板 ID
     * @return {@code CouponTemplateQueryRespDTO }
     */
    CouponTemplateQueryRespDTO findCouponTemplateById (String couponTemplateId);
    
    /**
     * 增加数量优惠券模板
     *
     * @param requestParam 请求参数
     */
    void increaseNumberCouponTemplate (CouponTemplateNumberReqDTO requestParam);
    
    /**
     * 终止优惠券模板
     *
     * @param couponTemplateId 优惠券模板 ID
     */
    void terminateCouponTemplate (TerminateCouponTemplateReqDTO requestParam);
    
    IPage<CouponTemplatePageQueryRespDTO> pageQueryCouponTemplate (CouponTemplatePageQueryReqDTO requestParam);
}
