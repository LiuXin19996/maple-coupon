package com.fengxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fengxin.dao.entity.CouponTaskDO;
import com.fengxin.dto.req.CouponTaskCreateReqDTO;

/**
 * @author FENGXIN
 * @date 2024/10/21
 * @project feng-coupon
 * @description 优惠券推送任务业务层
 **/
public interface CouponTaskService extends IService<CouponTaskDO>{
    void createCouponTask (CouponTaskCreateReqDTO requestParam);
}
