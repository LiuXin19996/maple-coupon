package com.fengxin.idempotent;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * @author FENGXIN
 * @date 2024/10/20
 * @project feng-coupon
 * @description 重复提交aop
 **/
@Aspect
@RequiredArgsConstructor
public class DuplicateSubmitAspect {
    private final RedissonClient redissonClient;
    
    /**
     * 重复提交方面
     *
     * @param joinPoint 加入点
     * @return {@code Object }
     * @throws Throwable 可投掷
     */
    @Around ("@annotation(com.fengxin.idempotent.DuplicateSubmit)")
    public Object duplicateSubmitAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        DuplicateSubmit duplicateSubmitAnnotation = getDuplicateSubmit (joinPoint);
        // 分布式🔒防止重复提交
        String lockKey = String.format ("lock_no_duplicate_submit:path%s:currentUserId:%smd5:%s",getRequestPath (),getCurrentUserId (),getMD5 (joinPoint));
        RLock lock = redissonClient.getLock (lockKey);
        if (!lock.tryLock ()) {
            throw new IllegalArgumentException (duplicateSubmitAnnotation.message());
        }
        Object result;
        try {
            result = joinPoint.proceed ();
        }finally {
            lock.unlock ();
        }
        return result;
        
    }
    
    /**
     * 获取重复提交注解
     */
    public DuplicateSubmit getDuplicateSubmit(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature ();
        Method declaredMethod = joinPoint.getTarget ().getClass ().getDeclaredMethod (signature.getName () , signature.getMethod ().getParameterTypes ());
        return declaredMethod.getAnnotation (DuplicateSubmit.class);
    }
    
    /**
     * 获取请求路径
     */
    public String getRequestPath(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes ();
        if (requestAttributes != null) {
            return requestAttributes.getRequest ().getServletPath ();
        }
        return null;
    }
    
    
    /**
     * 获取当前用户 ID
     */
    public String getCurrentUserId(){
        // TODO 用户属于非核心功能，这里先通过模拟的形式代替。后续如果需要后管展示，会重构该代码
        return "1810518709471555585";
    }
    
    /**
     * 获取 MD5
     *
     * @param joinPoint 加入点
     * @return {@code String }
     */
    public String getMD5(ProceedingJoinPoint joinPoint) {
        return DigestUtil.md5Hex(JSON.toJSONBytes(joinPoint.getArgs ()));
    }
}
