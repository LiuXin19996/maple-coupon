package com.fengxin.maplecoupon.distribution.mq.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fengxin.exception.ServiceException;
import com.fengxin.maplecoupon.distribution.common.enums.CouponTaskStatusEnum;
import com.fengxin.maplecoupon.distribution.common.enums.CouponTemplateStatusEnum;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTaskDO;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTaskExcelObject;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTaskMapper;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTemplateMapper;
import com.fengxin.maplecoupon.distribution.dao.mapper.UserCouponMapper;
import com.fengxin.maplecoupon.distribution.mq.design.CouponTaskExecuteEvent;
import com.fengxin.maplecoupon.distribution.mq.design.MessageWrapper;
import com.fengxin.maplecoupon.distribution.service.handler.excel.ReadExcelDistributionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 优惠券分发消费者
 **/
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "coupon_template_distribution_task_topic",
        consumerGroup = "mapleCoupon_distribution-message-execute-consumer"
)
@Slf4j(topic = "CouponTaskDistributionConsumer")
public class CouponTaskDistributionConsumer implements RocketMQListener<MessageWrapper<CouponTaskExecuteEvent>> {
    private final CouponTaskMapper couponTaskMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;
    private final StringRedisTemplate stringRedisTemplate;
    
    @Override
    public void onMessage (MessageWrapper<CouponTaskExecuteEvent> message) {
        // 日志
        log.info ("[消费者] 优惠券推送任务执行 消息体:{}",message.getMessage ());
        
        // 校验推送任务状态是否正常
        CouponTaskExecuteEvent couponTaskExecuteEvent = message.getMessage ();
        Long couponTaskId = couponTaskExecuteEvent.getCouponTaskId ();
        CouponTaskDO couponTaskDO = couponTaskMapper.selectById (couponTaskId);
        if (ObjectUtil.isNotNull (couponTaskDO) && ObjectUtil.notEqual (couponTaskDO.getStatus (), CouponTaskStatusEnum.IN_PROGRESS.getStatus ())) {
            log.warn ("[消费者] 优惠券任务 {} 推送异常：{} 终止推送",couponTaskId,couponTaskDO.getStatus ());
            return;
        }
        
        // 判断优惠券状态是否正确
        LambdaQueryWrapper<CouponTemplateDO> queryWrapper = Wrappers.<CouponTemplateDO>lambdaQuery ()
                .eq (CouponTemplateDO::getId,couponTaskDO.getCouponTemplateId ())
                .eq (CouponTemplateDO::getShopNumber,couponTaskDO.getShopNumber ())
                .eq (CouponTemplateDO::getStatus, CouponTemplateStatusEnum.ACTIVE.getValue ());
        CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne (queryWrapper);
        if (ObjectUtil.isNull (couponTemplateDO)) {
            log.warn ("[消费者] 优惠券 {}{} 状态异常：{} 终止推送",couponTemplateDO.getId (),couponTemplateDO.getName (),couponTemplateDO.getStatus ());
            return;
        }
        // 开始分发消费
        ReadExcelDistributionListener readExcelDistributionListener = new ReadExcelDistributionListener (
                couponTaskId ,
                stringRedisTemplate ,
                couponTaskMapper ,
                couponTemplateDO ,
                userCouponMapper ,
                couponTemplateMapper
        );
        EasyExcel.read (couponTaskDO.getFileAddress (), CouponTaskExcelObject.class,readExcelDistributionListener).sheet ().doRead ();
    }
}
