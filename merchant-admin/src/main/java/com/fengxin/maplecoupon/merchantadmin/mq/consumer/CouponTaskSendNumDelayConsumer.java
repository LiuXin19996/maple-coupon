package com.fengxin.maplecoupon.merchantadmin.mq.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.fengxin.maplecoupon.merchantadmin.dao.entity.CouponTaskDO;
import com.fengxin.maplecoupon.merchantadmin.dao.mapper.CouponTaskMapper;
import com.fengxin.exception.ServiceException;
import com.fengxin.maplecoupon.merchantadmin.service.impl.CouponTaskServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 延时队列消费者 刷新sendNum
 **/
@Service
@RequiredArgsConstructor
public class CouponTaskSendNumDelayConsumer implements CommandLineRunner {
    private final RedissonClient redissonClient;
    private final CouponTaskMapper couponTaskMapper;
    private final CouponTaskServiceImpl couponTaskService;
    @Override
    public void run (String... args) throws Exception {
        // 执行线程池刷新sendNum
        Executors.newSingleThreadExecutor (
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("delay_coupon-task_send-num_consumer");
                    thread.setDaemon(Boolean.TRUE);
                    return thread;
                }
        ).execute (() -> {
            RBlockingQueue<JSONObject> couponTaskSendNumDelayQueue = redissonClient.getBlockingQueue ("COUPON_TASK_SEND_NUM_DELAY_QUEUE");
            String couponTaskId = null;
            try {
                while (true) {
                    JSONObject take = couponTaskSendNumDelayQueue.take ();
                    couponTaskId = take.getString ("couponTaskId");
                    if (take != null) {
                        CouponTaskDO couponTaskDO = couponTaskMapper.selectById (couponTaskId);
                        if (couponTaskDO.getSendNum () == null) {
                            couponTaskService.refreshCouponTaskExcelRows (take);
                        }
                    }
                }
            } catch (Throwable e) {
                throw new ServiceException (String.format ("延时队列消费失败 couponTaskId: " + couponTaskId));
            }
        });
    }
}
