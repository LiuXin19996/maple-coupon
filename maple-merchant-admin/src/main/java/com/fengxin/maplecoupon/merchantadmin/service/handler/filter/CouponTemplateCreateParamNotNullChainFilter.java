package com.fengxin.maplecoupon.merchantadmin.service.handler.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fengxin.maplecoupon.merchantadmin.dto.req.CouponTemplateSaveReqDTO;
import com.fengxin.exception.ClientException;
import com.fengxin.maplecoupon.merchantadmin.service.basic.chain.MerchantAdminAbstractChainHandler;
import org.springframework.stereotype.Component;

import static com.fengxin.maplecoupon.merchantadmin.common.enums.ChainBizMarkEnum.MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 验证优惠券创建接口参数是否正确责任链｜验证必填参数是否为空或空的字符串
 **/
@Component
public class CouponTemplateCreateParamNotNullChainFilter implements MerchantAdminAbstractChainHandler<CouponTemplateSaveReqDTO> {
    
    @Override
    public void handler (CouponTemplateSaveReqDTO requestParam) {
        // 验证必填参数是否为空或空的字符串
        if (StrUtil.isEmpty(requestParam.getName())) {
            throw new ClientException ("优惠券名称不能为空");
        }
        
        if (ObjectUtil.isEmpty(requestParam.getSource())) {
            throw new ClientException("优惠券来源不能为空");
        }
        
        if (ObjectUtil.isEmpty(requestParam.getTarget())) {
            throw new ClientException ("优惠对象不能为空");
        }
        
        if (ObjectUtil.isEmpty(requestParam.getType())) {
            throw new ClientException("优惠类型不能为空");
        }
        
        if (ObjectUtil.isEmpty(requestParam.getValidStartTime())) {
            throw new ClientException("有效期开始时间不能为空");
        }
        
        if (ObjectUtil.isEmpty(requestParam.getValidEndTime())) {
            throw new ClientException("有效期结束时间不能为空");
        }
        
        if (ObjectUtil.isEmpty(requestParam.getStock())) {
            throw new ClientException("库存不能为空");
        }
        
        if (StrUtil.isEmpty(requestParam.getReceiveRule())) {
            throw new ClientException("领取规则不能为空");
        }
        
        if (StrUtil.isEmpty(requestParam.getConsumeRule())) {
            throw new ClientException("消耗规则不能为空");
        }
    }
    
    @Override
    public String mark () {
        return MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY.name();
    }
    
    @Override
    public int getOrder () {
        return 0;
    }
}
