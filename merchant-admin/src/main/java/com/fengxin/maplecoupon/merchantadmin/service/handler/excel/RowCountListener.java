package com.fengxin.maplecoupon.merchantadmin.service.handler.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;

/**
 * @author FENGXIN
 * @date 2024/10/21
 * @project feng-coupon
 * @description easyexcel获取excel行数
 **/
public class RowCountListener extends AnalysisEventListener<Object> {
    @Getter
    int rowCount = 0;
    
    @Override
    public void invoke (Object o , AnalysisContext analysisContext) {
        rowCount++;
    }
    
    @Override
    public void doAfterAllAnalysed (AnalysisContext analysisContext) {
    
    }
}
