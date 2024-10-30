package com.fengxin.maplecoupon.engine.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fengxin.maplecoupon.engine.common.constant.EngineRedisConstant;
import com.fengxin.maplecoupon.engine.common.context.UserContext;
import com.fengxin.maplecoupon.engine.mq.design.CanalBinlogEvent;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponDelayCloseEvent;
import com.fengxin.maplecoupon.engine.mq.producer.UserCouponDelayCloseProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

/**
 * @author FENGXIN
 * @date 2024/10/29
 * @project feng-coupon
 * @description canal异步消费
 **/
@RequiredArgsConstructor
@RocketMQMessageListener (
        topic = "maple-coupon_canal_engine-service_common-sync_topic-maple",
        consumerGroup = "maple-coupon_canal_engine-service_common-sync_consumer"
)
@Slf4j
public class CanalBinlogSyncUserCouponConsumer implements RocketMQListener<CanalBinlogEvent> {
    private final StringRedisTemplate stringRedisTemplate;
    private final UserCouponDelayCloseProducer userCouponDelayCloseProducer;
    @Override
    public void onMessage (CanalBinlogEvent message) {
        Map<String, Object> first = CollUtil.getFirst (message.getData ());
        String couponTemplateId = first.get ("coupon_template_id").toString ();
        String id = first.get ("id").toString ();
        // 用于监听用户优惠券创建事件
        if (ObjectUtil.equals (message.getType (),"INSERT")){
            // 添加用户领取优惠券模板缓存记录
            String userCouponListCacheKey = String.format(EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY, UserContext.getUserId());
            String userCouponItemCacheKey = StrUtil.builder()
                    .append(couponTemplateId)
                    .append("_")
                    .append(id)
                    .toString();
            DateTime now = DateUtil.parse (first.get ("receive_time").toString ());
            stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
            // 防止Redis宕机 造成数据丢失
            Double score;
            try {
                score = stringRedisTemplate.opsForZSet ().score (userCouponListCacheKey,userCouponItemCacheKey);
                // 插入失败 再插入一次
                if (ObjectUtil.isNull (score)){
                    // 如果这里还是失败 做不到万无一失 只能增加成功的概率
                    stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
                }
            }catch (Throwable e){
                log.warn("查询Redis用户优惠券记录为空或抛异常，可能Redis宕机或主从复制数据丢失，基础错误信息：{}", e.getMessage());
                // 如果直接抛异常大概率 Redis 宕机了，所以应该写个延时队列向 Redis 重试放入值。为了避免代码复杂性，这里直接写新增，大家知道最优解决方案即可
                stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
            }
            // 发送延时消息 结束用户优惠券
            UserCouponDelayCloseEvent userCouponDelayCloseEvent = UserCouponDelayCloseEvent.builder ()
                    .userCouponId (Long.valueOf (id))
                    .couponTemplateId (Long.valueOf (couponTemplateId))
                    .userId (Long.valueOf (UserContext.getUserId ()))
                    .delayDateTime (DateUtil.parse(first.get("valid_end_time").toString()).getTime())
                    .build ();
            SendResult sendResult = userCouponDelayCloseProducer.sendMessage (userCouponDelayCloseEvent);
            // 发送消息失败解决方案简单且高效的逻辑之一：打印日志并报警，通过日志搜集并重新投递
            if (ObjectUtil.notEqual(sendResult.getSendStatus().name(), "SEND_OK")) {
                log.warn("发送优惠券关闭延时队列失败，消息参数：{}", JSON.toJSONString(userCouponDelayCloseEvent));
            }
        }
    }
}
