package com.fengxin.maplecoupon.distribution.mq.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fengxin.idempotent.DuplicateMQConsume;
import com.fengxin.maplecoupon.distribution.common.enums.CouponTaskStatusEnum;
import com.fengxin.maplecoupon.distribution.common.enums.CouponTemplateStatusEnum;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTaskDO;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTaskExcelObject;
import com.fengxin.maplecoupon.distribution.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTaskFailMapper;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTaskMapper;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTemplateMapper;
import com.fengxin.maplecoupon.distribution.mq.design.CouponTaskExecuteEvent;
import com.fengxin.maplecoupon.distribution.mq.design.MessageWrapper;
import com.fengxin.maplecoupon.distribution.mq.producer.CouponExecuteDistributionProducer;
import com.fengxin.maplecoupon.distribution.service.handler.excel.ReadExcelDistributionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.fengxin.maplecoupon.distribution.common.constant.RocketMQConstant.*;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 优惠券分发消费者 只做进行 Excel 模板解析和前置校验
 **/
@Slf4j(topic = "CouponTaskDistributionConsumer")
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = COUPON_TASK_DISTRIBUTION_TOPIC,
        consumerGroup = COUPON_TASK_DISTRIBUTION_CONSUMER_GROUP
)
public class CouponTaskDistributionConsumer implements RocketMQListener<MessageWrapper<CouponTaskExecuteEvent>> {
    private final CouponTaskMapper couponTaskMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final CouponTaskFailMapper couponTaskFailMapper;
    private final CouponExecuteDistributionProducer couponExecuteDistributionProducer;
    
    @DuplicateMQConsume (
            keyPrefix = "mapleCoupon_idempotent:",
            key = "#messageWrapper.message.couponTaskId",
            timeout = 120
    )
    @Override
    public void onMessage (MessageWrapper<CouponTaskExecuteEvent> messageWrapper) {
        // 日志
        log.info ("[消费者] 优惠券推送任务执行 消息体:{}",messageWrapper.getMessage ());
        
        // 校验推送任务状态是否正常
        CouponTaskExecuteEvent couponTaskExecuteEvent = messageWrapper.getMessage ();
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
                couponTaskDO,
                stringRedisTemplate,
                couponTemplateDO,
                couponTaskFailMapper,
                couponExecuteDistributionProducer
        );
        EasyExcel.read (couponTaskDO.getFileAddress (), CouponTaskExcelObject.class,readExcelDistributionListener).sheet ().doRead ();
    }
}
