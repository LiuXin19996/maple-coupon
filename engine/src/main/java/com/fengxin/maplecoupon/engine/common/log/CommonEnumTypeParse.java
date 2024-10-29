package com.fengxin.maplecoupon.engine.common.log;

import cn.hutool.core.util.StrUtil;
import com.fengxin.maplecoupon.engine.common.enums.DiscountTargetEnum;
import com.fengxin.maplecoupon.engine.common.enums.DiscountTypeEnum;
import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author FENGXIN
 * @date 2024/10/19
 * @project feng-coupon
 * @description 将优惠对象和优惠类型转换为相应名称存入日志记录
 **/
@Slf4j
@Component
public class CommonEnumTypeParse implements IParseFunction {
    // 优惠类型
    private static final String DISCOUNT_TYPE_NAME = DiscountTypeEnum.class.getSimpleName();
    // 优惠范围
    private static final String DISCOUNT_TARGET_NAME = DiscountTargetEnum.class.getSimpleName();
    
    @Override
    public String functionName () {
        return "COMMON_PARSE_DISCOUNT";
    }
    
    @Override
    public String apply (Object value) {
        try {
            List<String> split = StrUtil.split (value.toString () , "_");
            if (split.size () != 2) {
                throw new IllegalArgumentException ("格式错误 请确保格式为 “类型名_类型值”");
            }
            String enumClassName = split.get (0);
            String enumValue = split.get (1);
            return parseFunction (enumClassName , enumValue);
        }catch (NumberFormatException e) {
            throw new IllegalArgumentException ("_后的值必须是正整数" + e.getMessage());
        }
    }
    
    public String parseFunction (String enumClassName, String enumValue) {
        if (DISCOUNT_TYPE_NAME.equals (enumClassName)) {
            log.info (enumClassName,DISCOUNT_TYPE_NAME);
            return DiscountTypeEnum.findValueByType (Integer.parseInt (enumValue));
        }else if (DISCOUNT_TARGET_NAME.equals (enumClassName)) {
            log.info (enumClassName, DISCOUNT_TARGET_NAME);
            return DiscountTargetEnum.findValueByType (Integer.parseInt (enumValue));
        }else {
            throw new NoSuchElementException ("未知的枚举名");
        }
    }
}
