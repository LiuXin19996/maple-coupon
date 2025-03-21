package com.fengxin.maplecoupon.merchantadmin.service.basic.log;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fengxin
 * @date 2025-03-22
 * @description 策略工厂
 */
@Component
public class LogRecordStrategyFactory implements ApplicationContextAware {
    private final Map<String, LogRecordStrategy> strategyMap = new ConcurrentHashMap<> ();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        applicationContext.getBeansOfType(LogRecordStrategy.class)
                .values()
                .forEach(strategy -> strategyMap.put(strategy.getType(), strategy));
    }

    public LogRecordStrategy getStrategy(String type) {
        return strategyMap.get(type);
    }
}