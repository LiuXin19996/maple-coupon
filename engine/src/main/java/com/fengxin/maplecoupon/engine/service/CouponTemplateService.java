package com.fengxin.maplecoupon.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.fengxin.maplecoupon.engine.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateQueryReqDTO;
import com.fengxin.maplecoupon.engine.dto.req.CouponTemplateRemindTimeReqDTO;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateQueryRespDTO;

import java.util.List;


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
     * 按 ID 和商店编号列出优惠券模板
     *
     * @param couponTemplateIdList 优惠券模板 ID 列表
     * @param shopNumberList       店铺编号一览
     * @return {@code List<CouponTemplateDO> }
     */
    List<CouponTemplateDO> listCouponTemplateByIdAndShopNumber (List<Long> couponTemplateIdList , List<Long> shopNumberList);
}
