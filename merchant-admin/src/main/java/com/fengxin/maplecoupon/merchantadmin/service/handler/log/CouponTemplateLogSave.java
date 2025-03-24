package com.fengxin.maplecoupon.merchantadmin.service.handler.log;

import com.fengxin.maplecoupon.merchantadmin.service.basic.log.LogRecordStrategy;
import com.fengxin.maplecoupon.merchantadmin.service.basic.log.LogRecordStrategyFactory;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author maple
 * @date 2025-03-22
 * @description 主服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponTemplateLogSave implements ILogRecordService {
    
    private final LogRecordStrategyFactory strategyFactory;

    @Override
    public void record(LogRecord logRecord) {
        try {
            LogRecordStrategy strategy = strategyFactory.getStrategy(logRecord.getType());
            if (strategy != null) {
                strategy.handleLogRecord(logRecord);
            } else {
                log.warn("Unsupported log record type: {}", logRecord.getType());
            }
        } catch (Exception e) {
            log.error("日志{}插入失败{}", logRecord.getType(), e.getMessage());
        }
    }
    
    @Override
    public List<LogRecord> queryLog(String bizNo, String type) {
        return List.of();
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
        return List.of();
    }
}