package com.fengxin.service.handler.filter;

import cn.hutool.core.util.ObjectUtil;
import com.fengxin.common.enums.DiscountTargetEnum;
import com.fengxin.dto.req.CouponTemplateSaveReqDTO;
import com.fengxin.service.basic.chain.MerchantAdminAbstractChainHandler;
import org.springframework.stereotype.Component;

import static com.fengxin.common.enums.ChainBizMarkEnum.MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 验证优惠券创建接口参数是否正确责任链｜验证参数数据是否正确
 **/
@Component
public class CouponTemplateCreateParamVerifyChainFilter implements MerchantAdminAbstractChainHandler<CouponTemplateSaveReqDTO> {
    
    @Override
    public void handler (CouponTemplateSaveReqDTO requestParam) {
        
        // 验证参数数据是否正确
        if (ObjectUtil.equal(requestParam.getTarget(), DiscountTargetEnum.PRODUCT_SPECIFIC)) {
            // 调用商品中台验证商品是否存在，如果不存在抛出异常
            // ......
        }
    }
    
    @Override
    public String mark () {
        return MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY.name();
    }
    
    @Override
    public int getOrder () {
        return 20;
    }
}
