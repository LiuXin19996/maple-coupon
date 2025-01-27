package com.fengxin.maplecoupon.engine.service.handler.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.fengxin.maplecoupon.engine.common.enums.CouponRemindTypeEnum;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponRemindEvent;
import com.fengxin.maplecoupon.engine.mq.producer.UserCouponRemindProducer;
import com.fengxin.maplecoupon.engine.service.UserCouponService;
import com.fengxin.maplecoupon.engine.service.handler.dto.CouponTemplateRemindDTO;
import com.fengxin.maplecoupon.engine.service.handler.service.RemindUserCouponTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.*;

import static com.fengxin.maplecoupon.engine.common.constant.EngineRedisConstant.COUPON_REMIND_CHECK_KEY;

/**
 * 执行提醒用户优惠券模板 impl
 *
 * @author fengxin
 * @date 2024-10-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RemindUserCouponTemplateImpl implements RemindUserCouponTemplate {
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final SendAppMessageRemindCouponTemplate sendAppMessageRemindCouponTemplate;
    private final SendSMSMessageRemindCouponTemplate sendSMSMessageRemindCouponTemplate;
    private final UserCouponService userCouponService;
    // 线程池并行处理 提高效率
    private final ExecutorService executorService = new ThreadPoolExecutor(
            Runtime.getRuntime ().availableProcessors () << 1 ,
            Runtime.getRuntime ().availableProcessors ()  << 2,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<> (),
            new ThreadPoolExecutor.CallerRunsPolicy ()
    );
    public static final String REDIS_BLOCKING_DEQUE = "COUPON_REMIND_QUEUE";
    
    @Override
    public void executeRemindUserCoupon (CouponTemplateRemindDTO couponTemplateRemindDTO) {
        if (userCouponService.isCanalRemind (couponTemplateRemindDTO)){
            log.info ("用户[{}]已经取消优惠券{}的预约提醒",couponTemplateRemindDTO.getUserId (),couponTemplateRemindDTO.getCouponTemplateId ());
            return;
        }
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque (REDIS_BLOCKING_DEQUE);
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue (blockingDeque);
        // redis延时队列key
        String key = String.format (COUPON_REMIND_CHECK_KEY,
                couponTemplateRemindDTO.getCouponTemplateId (),
                couponTemplateRemindDTO.getShopNumber (),
                couponTemplateRemindDTO.getRemindTime (),
                couponTemplateRemindDTO.getType ());
        stringRedisTemplate.opsForValue ().set (key, JSON.toJSONString (couponTemplateRemindDTO));
        delayedQueue.offer (key,10,TimeUnit.SECONDS);
        
        executorService.execute (() -> {
            switch (Objects.requireNonNull (CouponRemindTypeEnum.getByType (couponTemplateRemindDTO.getType ()))){
                case APP -> sendAppMessageRemindCouponTemplate.remind(couponTemplateRemindDTO);
                case SMS -> sendSMSMessageRemindCouponTemplate.remind(couponTemplateRemindDTO);
                default -> {}
            }
            // 消费完后删除提醒key
            stringRedisTemplate.delete (key);
        });
    }
    
    @Slf4j
    @Component
    @RequiredArgsConstructor
    static class executeRemindDelay implements CommandLineRunner {
        private final UserCouponRemindProducer userCouponRemindProducer;
        private final RedissonClient redissonClient;
        private final StringRedisTemplate stringRedisTemplate;
        
        @Override
        public void run (String... args) throws Exception {
            Executors.newSingleThreadExecutor (
                    runnable -> {
                        Thread thread = new Thread ();
                        thread.setName("delay_coupon-remind_consumer");
                        thread.setDaemon(Boolean.TRUE);
                        return thread;
                    }
            ).execute (() -> {
                RBlockingDeque<String> blockingDeque = redissonClient.getBlockingDeque (REDIS_BLOCKING_DEQUE);
                while (true){
                    try {
                        String key = blockingDeque.take ();
                        // key存在 说明消费失败 重新投递消息消费
                        if (stringRedisTemplate.hasKey (key)){
                            log.info("检查用户发送的通知消息Key：{} 未消费完成，开启重新投递", key);
                            
                            // Redis 中还存在该 Key，说明任务没被消费完，则可能是消费机器宕机了，重新投递消息
                            UserCouponRemindEvent userCouponRemindEvent = JSONUtil.toBean (stringRedisTemplate.opsForValue ().get (key) , UserCouponRemindEvent.class);
                            userCouponRemindProducer.sendMessage(userCouponRemindEvent);
                            // 提醒用户后删除 Key
                            stringRedisTemplate.delete(key);
                        }
                    } catch (Throwable e) {
                        log.error ("redis延时队列异常");
                    }
                }
            });
        }
    }
}
