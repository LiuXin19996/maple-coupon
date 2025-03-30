package com.fengxin.maplecoupon.distribution.service.handler.excel;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson2.JSON;
import com.fengxin.maplecoupon.distribution.dao.entity.*;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTaskFailMapper;
import com.fengxin.maplecoupon.distribution.mq.design.CouponTemplateDistributionEvent;
import com.fengxin.maplecoupon.distribution.mq.producer.CouponExecuteDistributionProducer;
import com.fengxin.maplecoupon.distribution.util.StockDecrementReturnCombinedUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Map;

import static com.fengxin.maplecoupon.distribution.common.constant.DistributionRedisConstant.TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY;
import static com.fengxin.maplecoupon.distribution.common.constant.DistributionRedisConstant.TEMPLATE_TASK_EXECUTE_PROGRESS_KEY;
import static com.fengxin.maplecoupon.distribution.common.constant.EngineRedisConstant.COUPON_TEMPLATE_KEY;

/**
 * @author FENGXIN
 * @date 2024/10/24
 * @project feng-coupon
 * @description 优惠券任务读取 Excel 分发监听器
 **/
@RequiredArgsConstructor
@Slf4j
public class ReadExcelDistributionListener extends AnalysisEventListener<CouponTaskExcelObject> {
    private final CouponTaskDO couponTaskDO;
    private final StringRedisTemplate stringRedisTemplate;
    private final CouponTemplateDO couponTemplateDO;
    private final CouponTaskFailMapper couponTaskFailMapper;
    private final CouponExecuteDistributionProducer couponExecuteDistributionProducer;
    
    private int rowCount = 1;
    private int lastSavedRowCount = 0;
    private static final int BATCH_SAVE_PROGRESS_SIZE = 100000;
    private final static String STOCK_DECREMENT_AND_BATCH_SAVE_USER_RECORD_LUA_PATH = "lua/stock_decrement_and_batch_save_user_record.lua";
    private final static int BATCH_USER_COUPON_SIZE = 100000;
    
    @PostConstruct
    public void init() {
        // 初始化时读取Redis进度
        String progressKey = String.format(TEMPLATE_TASK_EXECUTE_PROGRESS_KEY, couponTaskDO.getId());
        String progress = stringRedisTemplate.opsForValue().get(progressKey);
        lastSavedRowCount = StrUtil.isNotBlank(progress) ? Integer.parseInt(progress) : 0;
        // 设置EasyExcel从lastSavedRowCount + 1行开始读取 跳过已经执行过的行数
        rowCount = lastSavedRowCount + 1;
    }
    
    @Override
    public void invoke (CouponTaskExcelObject couponTaskExcelObject , AnalysisContext analysisContext) {
        if (lastSavedRowCount >= rowCount) {
            ++rowCount;
            return;
        }
        Long couponTaskId = couponTaskDO.getId ();
        // 获取lua脚本 将脚本保存到Hutool单例容器 减少获取脚本耗时
        DefaultRedisScript<Long> luaScript = Singleton.get (STOCK_DECREMENT_AND_BATCH_SAVE_USER_RECORD_LUA_PATH , () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<> ();
            redisScript.setScriptSource (new ResourceScriptSource (new ClassPathResource (STOCK_DECREMENT_AND_BATCH_SAVE_USER_RECORD_LUA_PATH)));
            redisScript.setResultType (Long.class);
            return redisScript;
        });
        // 执行 LUA 脚本进行扣减库存以及增加 Redis 用户领券记录
        String couponTemplateIdKey = String.format (COUPON_TEMPLATE_KEY , couponTemplateDO.getId ());
        String userSetKey = String.format (TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY , couponTaskId);
        Map<Object, Object> userRowNumMap = MapUtil.builder ()
                .put ("userId" , couponTaskExcelObject.getUserId ())
                .put ("rowNum" , rowCount + 1)
                .build ();
        Long executeResult = stringRedisTemplate.execute (luaScript , ListUtil.of (couponTemplateIdKey , userSetKey) , JSON.toJSONString (userRowNumMap));
        // firstField 为 false 说明优惠券已经没有库存了
        if (!StockDecrementReturnCombinedUtil.extractFirstField (executeResult)){
            // 保存进度到缓存
            saveProgressWithBatch(rowCount);
            // 保存失败原因到数据库
            Map<Object, Object> failMap = MapUtil.builder ()
                    .put ("rowNum" , rowCount)
                    .put ("cause" , "优惠券库存不足")
                    .build ();
            CouponTaskFailDO failDO = CouponTaskFailDO.builder ()
                    .batchId (couponTaskDO.getBatchId ())
                    .jsonObject (JSON.toJSONString (failMap))
                    .build ();
            couponTaskFailMapper.insert (failDO);
            return;
        }
        // 获取执行后的用户set行数 未达到分发数量则保存进度到缓存 继续下一条记录执行
        int batchSize = StockDecrementReturnCombinedUtil.extractSecondField ((executeResult.intValue ()));
        if (batchSize % BATCH_USER_COUPON_SIZE != 0){
            saveProgressWithBatch(rowCount);
            return;
        }
        // 分发
        CouponTemplateDistributionEvent couponTemplateDistributionEvent = CouponTemplateDistributionEvent.builder ()
                .couponTemplateId (couponTemplateDO.getId ())
                .shopNumber (couponTaskDO.getShopNumber ())
                .couponTaskId (couponTaskId)
                .couponTaskBatchId (couponTaskDO.getBatchId ())
                .notifyType (couponTaskDO.getNotifyType ())
                .phone (couponTaskExcelObject.getPhone ())
                .mail (couponTaskExcelObject.getMail ())
                .userId (couponTaskExcelObject.getUserId ())
                .batchUserSetSize (batchSize)
                .validStartTime (couponTemplateDO.getValidStartTime ())
                .validEndTime (couponTemplateDO.getValidEndTime ())
                .couponTemplateConsumeRule (couponTemplateDO.getConsumeRule ())
                // 分发未到最后一批
                .distributionEndFlag (Boolean.FALSE)
                .build ();
        couponExecuteDistributionProducer.sendMessage (couponTemplateDistributionEvent);
        // 保存进度
        saveProgressWithBatch(rowCount);
    }
    
    @Override
    public void doAfterAllAnalysed (AnalysisContext analysisContext) {
        // 处理结束时强制保存进度
        if (rowCount - 1 > lastSavedRowCount) {
            saveProgressImmediately(rowCount - 1);
        }
        //  Excel 读取到最后可能没满足 100000 批量也需要发送消息队列
        CouponTemplateDistributionEvent couponTemplateDistributionEvent = CouponTemplateDistributionEvent.builder ()
                .couponTemplateId (couponTemplateDO.getId ())
                .shopNumber (couponTaskDO.getShopNumber ())
                .couponTaskId (couponTaskDO.getId ())
                .couponTaskBatchId (couponTaskDO.getBatchId ())
                .notifyType (couponTaskDO.getNotifyType ())
                .couponTemplateConsumeRule (couponTemplateDO.getConsumeRule ())
                // 分发到最后一批
                .distributionEndFlag (Boolean.TRUE)
                .build ();
        couponExecuteDistributionProducer.sendMessage (couponTemplateDistributionEvent);
    }
    
    /**
     * 保存进度到本地缓存和Redis
     * @param currentRow 当前进度
     */
    private void saveProgressImmediately(int currentRow) {
        String progressKey = String.format(TEMPLATE_TASK_EXECUTE_PROGRESS_KEY, couponTaskDO.getId());
        stringRedisTemplate.opsForValue().set(progressKey, String.valueOf(currentRow));
        lastSavedRowCount = currentRow;
    }
    
    /**
     * 批量保存进度到本地缓存
     * @param currentRow 当前进度
     */
    private void saveProgressWithBatch(int currentRow) {
        // 达到批量阈值或结束时保存
        if (currentRow - lastSavedRowCount >= BATCH_SAVE_PROGRESS_SIZE) {
            saveProgressImmediately(currentRow);
        }
        ++rowCount;
    }
}
