package com.fengxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fengxin.dao.entity.CouponTemplateDO;
import com.fengxin.dto.req.CouponTemplateSaveReqDTO;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 优惠券模板业务接口
 **/
public interface CouponTemplateService extends IService<CouponTemplateDO> {
    void createCouponTemplate (CouponTemplateSaveReqDTO requestParam);
}
