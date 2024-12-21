package com.fengxin.maplecoupon.merchantadmin.controller;

import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTaskCreateReqDTO;
import com.fengxin.idempotent.DuplicateSubmit;
import com.fengxin.maplecoupon.merchantadmin.service.CouponTaskService;
import com.fengxin.web.Result;
import com.fengxin.web.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author FENGXIN
 * @date 2024/10/21
 * @project feng-coupon
 * @description 优惠券推送任务控制层
 **/
@RestController
@RequiredArgsConstructor
@Tag (name = "优惠券推送任务管理")
public class CouponTaskController {
    
    private final CouponTaskService couponTaskService;
    
    @Operation(summary = "创建优惠券推送任务")
    // @DuplicateSubmit(message = "请勿短时间内重复提交优惠券推送任务")
    @PostMapping("/api/merchant-admin/coupon-task/create")
    public Result<Void> createCouponTask(@RequestBody CouponTaskCreateReqDTO requestParam) {
        couponTaskService.createCouponTask(requestParam);
        return Results.success();
    }
}
