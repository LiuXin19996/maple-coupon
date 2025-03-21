package com.fengxin.maplecoupon.merchantadmin.service.basic.log;

import cn.hutool.core.util.StrUtil;
import com.fengxin.maplecoupon.merchantadmin.common.context.UserContext;
import com.fengxin.maplecoupon.merchantadmin.dao.entity.CouponTemplateLogDO;
import com.fengxin.maplecoupon.merchantadmin.dao.mapper.CouponTemplateLogMapper;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.context.LogRecordContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author fengxin
 * @date 2025-03-22
 * @description CouponTemplate 类型策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponTemplateLogStrategy implements LogRecordStrategy {
    private final CouponTemplateLogMapper couponTemplateLogMapper;

    @Override
    public String getType() {
        return "CouponTemplate";
    }

    @Override
    public void handleLogRecord(LogRecord logRecord) {
        CouponTemplateLogDO couponTemplateLogDO = CouponTemplateLogDO.builder()
                .couponTemplateId(logRecord.getBizNo())
                .shopNumber(UserContext.getShopNumber())
                .operatorId(UserContext.getUserId())
                .operationLog(logRecord.getAction())
                .originalData(Optional.ofNullable(LogRecordContext.getVariable("originalData"))
                         .map(Object::toString).orElse(""))
                .modifiedData(StrUtil.isBlank(logRecord.getExtra()) ? null : logRecord.getExtra())
                .build();
        couponTemplateLogMapper.insert(couponTemplateLogDO);
    }
}
