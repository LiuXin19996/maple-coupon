package com.fengxin.maplecoupon.distribution.dao.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author FENGXIN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponTaskFailExcelObject {
    
    @ColumnWidth(20)
    @ExcelProperty("行数")
    private Integer rowNum;
    
    @ColumnWidth(30)
    @ExcelProperty("错误原因")
    private String cause;
    
}
