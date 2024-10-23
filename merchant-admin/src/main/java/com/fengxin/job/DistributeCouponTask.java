package com.fengxin.job;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fengxin.common.enums.CouponTaskSendTypeEnum;
import com.fengxin.common.enums.CouponTaskStatusEnum;
import com.fengxin.dao.entity.CouponTaskDO;
import com.fengxin.dao.mapper.CouponTaskMapper;
import com.fengxin.dto.mq.CouponTaskExecuteEvent;
import com.fengxin.mq.producer.CouponTemplateTaskProducer;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author FENGXIN
 * @date 2024/10/23
 * @project feng-coupon
 * @description XXL-JOB定时分发优惠券模板
 **/
@Component
@RequiredArgsConstructor
public class DistributeCouponTask {
    private final CouponTaskMapper couponTaskMapper;
    private final CouponTemplateTaskProducer couponTemplateTaskProducer;
    private static final int MAX_LIMIT = 100;
    
    @XxlJob ("couponTemplateTask")
    public void execute() {
        long startId = 0;
        Date now = new Date ();
        while (true) {
            // 获取任务列表
            List<CouponTaskDO> couponTaskDOList = fetchPendingTasks (startId , now);
            if (CollUtil.isEmpty (couponTaskDOList)) {
                break;
            }
            for (CouponTaskDO couponTaskDO : couponTaskDOList) {
                sendCouponTemplate (couponTaskDO);
            }
            
            // 校验是否为最后一批
            if (couponTaskDOList.size() < MAX_LIMIT) {
                break;
            }
            // 获取最大id 方便下一次开始分发
            startId = couponTaskDOList.stream ()
                    .mapToLong (CouponTaskDO::getId)
                    .max ()
                    .getAsLong ();
        }
    }
    
    /**
     * 获取待处理任务
     *
     * @param startId 起始 ID
     * @param now     现在
     * @return {@code List<CouponTaskDO> }
     */
    public List<CouponTaskDO> fetchPendingTasks(long startId, Date now){
        LambdaQueryWrapper<CouponTaskDO> queryWrapper = Wrappers.<CouponTaskDO>lambdaQuery()
                .gt (CouponTaskDO::getId, startId)
                .eq (CouponTaskDO::getSendType, CouponTaskSendTypeEnum.SCHEDULED.getType ())
                .le(CouponTaskDO::getSendTime, now)
                .last("LIMIT " + MAX_LIMIT);
        return couponTaskMapper.selectList(queryWrapper);
    }
    
    /**
     * 发送优惠券模板
     *
     * @param couponTaskDO Coupon 任务执行
     */
    public void sendCouponTemplate(CouponTaskDO couponTaskDO ){
        // 设置状态为发送中
        CouponTaskDO taskDO = CouponTaskDO.builder ()
                .id (couponTaskDO.getId ())
                .status (CouponTaskStatusEnum.IN_PROGRESS.getStatus ())
                .build ();
        couponTaskMapper.updateById(taskDO);
        // 消息队列生产者发送 分发服务消费者消费
        CouponTaskExecuteEvent couponTaskExecuteEvent = CouponTaskExecuteEvent.builder ()
                .couponTaskId (couponTaskDO.getId ())
                .build ();
        couponTemplateTaskProducer.sendMessage (couponTaskExecuteEvent);
    }
}
