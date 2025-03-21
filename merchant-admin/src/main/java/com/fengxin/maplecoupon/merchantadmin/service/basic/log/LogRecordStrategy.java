package com.fengxin.maplecoupon.merchantadmin.service.basic.log;

import com.mzt.logapi.beans.LogRecord;

/**
 * @author fengxin
 * @date 2025-03-22
 * @description 策略接口定义
 */
public interface LogRecordStrategy {
    String getType();
    void handleLogRecord(LogRecord logRecord);
}
