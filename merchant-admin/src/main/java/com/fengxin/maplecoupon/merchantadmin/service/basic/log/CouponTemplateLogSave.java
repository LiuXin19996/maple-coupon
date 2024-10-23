package com.fengxin.maplecoupon.merchantadmin.service.basic.log;

import cn.hutool.core.util.StrUtil;
import com.fengxin.maplecoupon.merchantadmin.common.context.UserContext;
import com.fengxin.maplecoupon.merchantadmin.dao.entity.CouponTemplateLogDO;
import com.fengxin.maplecoupon.merchantadmin.dao.mapper.CouponTemplateLogMapper;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.ILogRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author FENGXIN
 * @date 2024/10/19
 * @project feng-coupon
 * @description 日志持久化
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponTemplateLogSave implements ILogRecordService {
    
    private final CouponTemplateLogMapper couponTemplateLogMapper;
    
    @Override
    public void record (LogRecord logRecord) {
        try {
            switch (logRecord.getType ()){
                case "CouponTemplate" :{
                    CouponTemplateLogDO couponTemplateLogDO = CouponTemplateLogDO.builder ()
                            .couponTemplateId (logRecord.getBizNo ())
                            .shopNumber (UserContext.getShopNumber ())
                            .operatorId (UserContext.getUserId ())
                            .operationLog (logRecord.getAction ())
                            .originalData (Optional.ofNullable (LogRecordContext.getVariable ("originalData")).map (Object::toString).orElse (""))
                            .modifiedData (StrUtil.isBlank (logRecord.getExtra ()) ? null : logRecord.getExtra ())
                            .build ();
                    couponTemplateLogMapper.insert (couponTemplateLogDO);
                }
            }
        } catch (Exception e) {
            log.error ("日志{}插入失败{}" , e.getMessage () , logRecord.getType ());
        }
    }
    
    @Override
    public List<LogRecord> queryLog (String bizNo , String type) {
        return List.of ();
    }
    
    @Override
    public List<LogRecord> queryLogByBizNo (String bizNo , String type , String subType) {
        return List.of ();
    }
}
