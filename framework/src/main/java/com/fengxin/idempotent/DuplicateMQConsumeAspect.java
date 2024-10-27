package com.fengxin.idempotent;

import cn.hutool.core.util.ObjectUtil;
import com.fengxin.enums.MQConsumeStatusEnum;
import com.fengxin.exception.ServiceException;
import com.fengxin.util.SpELUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author FENGXIN
 * @date 2024/10/27
 * @project feng-coupon
 * @description 解决幂等消费
 **/
@Slf4j
@Aspect
@RequiredArgsConstructor
public class DuplicateMQConsumeAspect {
    private final StringRedisTemplate stringRedisTemplate;
    
    private static final String LUA_SCRIPT = """
            local key = KEYS[1]
            local value = ARGV[1]
            local expire_time_ms = ARGV[2]
            return redis.call('SET', key, value, 'NX', 'GET', 'PX', expire_time_ms)
            """;
    @Around ("@annotation(com.fengxin.idempotent.DuplicateMQConsume)")
    public Object duplicateMQConsumeExecute (ProceedingJoinPoint joinPoint) throws Throwable {
        DuplicateMQConsume duplicateMQConsume = getDuplicateMQConsumeAnnotation (joinPoint);
        String uniqueKey = duplicateMQConsume.keyPrefix() + SpELUtil.parseKey(duplicateMQConsume.key(), ((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getArgs());
        // 设置状态为执行中
        String absentAndGet = stringRedisTemplate.execute (
                RedisScript.of (LUA_SCRIPT , String.class) ,
                List.of (uniqueKey) ,
                MQConsumeStatusEnum.CONSUMING.getCode(),
                String.valueOf (TimeUnit.SECONDS.toMillis (duplicateMQConsume.timeout ()))
        );
        // 返回值不空 说明有值
        if (ObjectUtil.isNotNull (absentAndGet)) {
            // 如果返回0说明正在执行
            boolean errorFlag = MQConsumeStatusEnum.isError(absentAndGet);
            log.warn("[{}] MQ repeated consumption, {}.", uniqueKey, errorFlag ? "Wait for the client to delay consumption" : "Status is completed");
            if (errorFlag) {
                throw new ServiceException (String.format("消息消费者幂等异常，幂等标识：%s", uniqueKey));
            }
            // 执行完毕 直接返回
            return null;
        }
        Object result;
        try {
            result = joinPoint.proceed ();
            // 设置防重令牌 Key 消费完成 过期时间，单位秒
            stringRedisTemplate.opsForValue().set(uniqueKey, MQConsumeStatusEnum.CONSUMED.getCode(), duplicateMQConsume.timeout (), TimeUnit.SECONDS);
        }catch (Throwable e) {
            // 删除幂等 Key，让消息队列消费者重试逻辑进行重新消费
            stringRedisTemplate.delete(uniqueKey);
            throw e;
        }
        return result;
    }
    
    /**
     * 获取重复注解
     *
     * @param joinPoint 切入点
     * @return {@code DuplicateMQConsume 注解}
     * @throws NoSuchMethodException 异常
     */
    public static DuplicateMQConsume getDuplicateMQConsumeAnnotation (JoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature ();
        Method declaredMethod = joinPoint.getTarget ().getClass ().getDeclaredMethod (signature.getName () , signature.getMethod ().getParameterTypes ());
        return declaredMethod.getAnnotation(DuplicateMQConsume.class);
    }
}
