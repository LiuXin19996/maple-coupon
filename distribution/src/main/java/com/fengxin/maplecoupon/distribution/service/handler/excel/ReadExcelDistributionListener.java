package com.fengxin.maplecoupon.distribution.service.handler.excel;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.fengxin.maplecoupon.distribution.common.enums.CouponSourceEnum;
import com.fengxin.maplecoupon.distribution.common.enums.CouponStatusEnum;
import com.fengxin.maplecoupon.distribution.common.enums.CouponTaskStatusEnum;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTaskDO;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTaskExcelObject;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.distribution.dao.entity.UserCouponDO;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTaskMapper;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTemplateMapper;
import com.fengxin.maplecoupon.distribution.dao.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchExecutorException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;

import static com.fengxin.maplecoupon.distribution.common.constant.EngineRedisConstant.COUPON_TEMPLATE_KEY;
import static com.fengxin.maplecoupon.distribution.common.constant.EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY;

/**
 * @author FENGXIN
 * @date 2024/10/24
 * @project feng-coupon
 * @description 优惠券任务读取 Excel 分发监听器
 **/
@RequiredArgsConstructor
@Slf4j
public class ReadExcelDistributionListener extends AnalysisEventListener<CouponTaskExcelObject> {
    private final Long couponTaskId;
    private final StringRedisTemplate stringRedisTemplate;
    private final CouponTaskMapper couponTaskMapper;
    private final CouponTemplateDO couponTemplateDO;
    private final UserCouponMapper userCouponMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    @Override
    public void invoke (CouponTaskExcelObject couponTaskExcelObject , AnalysisContext analysisContext) {
        // 扣减缓存优惠券模板数量是否足够
        long decrementStock = stringRedisTemplate.opsForHash ().increment (String.format (COUPON_TEMPLATE_KEY , couponTemplateDO.getId ()) , "stock" , -1);
        if (decrementStock < 0){
            log.error ("优惠券库存不足{}",decrementStock);
            return;
        }
        // 扣减数据库优惠券模板数量
        int decrement = couponTemplateMapper.decrementCouponTemplateStock (couponTemplateDO.getShopNumber () , couponTemplateDO.getId () , 1);
        if (!SqlHelper.retBool (decrement)){
            log.error ("优惠券库存不足{}",decrement);
            return;
        }
        // 添加用户领券记录到数据库
        Date date = new Date ();
        DateTime validEndTime = DateUtil.offsetHour(date, JSON.parseObject(couponTemplateDO.getConsumeRule()).getInteger("validityPeriod"));
        UserCouponDO userCouponDO = UserCouponDO.builder ()
                .couponTemplateId (couponTemplateDO.getId ())
                .userId (Long.valueOf (couponTaskExcelObject.getUserId ()))
                .receiveCount (1)
                .source (CouponSourceEnum.PLATFORM.getType ())
                .status (CouponStatusEnum.EFFECTIVE.getType ())
                .receiveTime (date)
                .createTime (date)
                .updateTime (date)
                .validStartTime (date)
                .validEndTime (validEndTime)
                .delFlag (0)
                .build ();
        try {
            userCouponMapper.insert (userCouponDO);
        } catch (BatchExecutorException bee) {
            log.error ("用户优惠券不可重复领取优惠券");
            return;
        }
        // 添加优惠券到用户已领取的 Redis 优惠券列表中
        String cacheUserCouponTemplateList = String.format (USER_COUPON_TEMPLATE_LIST_KEY , couponTaskExcelObject.getUserId ());
        String cacheUserCouponTemplate = StrUtil.builder ()
                .append (couponTemplateDO.getId ())
                .append ("-")
                .append (userCouponDO.getId ())
                .toString ();
        // 将领券时间作为 Score 值，这样用户在查询时可以按时间倒序显示领取记录
        stringRedisTemplate.opsForZSet ().add (cacheUserCouponTemplateList,cacheUserCouponTemplate,date.getTime());
    }
    
    @Override
    public void doAfterAllAnalysed (AnalysisContext analysisContext) {
        // 确保所有用户都已经接到优惠券后，设置优惠券推送任务完成时间
        CouponTaskDO couponTaskDO = CouponTaskDO.builder()
                .id(couponTaskId)
                .status(CouponTaskStatusEnum.SUCCESS.getStatus())
                .completionTime(new Date())
                .build();
        couponTaskMapper.updateById(couponTaskDO);
    }
}
