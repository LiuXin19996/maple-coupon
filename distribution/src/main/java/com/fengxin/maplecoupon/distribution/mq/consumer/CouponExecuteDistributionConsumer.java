package com.fengxin.maplecoupon.distribution.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fengxin.maplecoupon.distribution.common.constant.EngineRedisConstant;
import com.fengxin.maplecoupon.distribution.common.enums.CouponSourceEnum;
import com.fengxin.maplecoupon.distribution.common.enums.CouponStatusEnum;
import com.fengxin.maplecoupon.distribution.common.enums.CouponTaskStatusEnum;
import com.fengxin.maplecoupon.distribution.dao.entity.*;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTaskFailMapper;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTaskMapper;
import com.fengxin.maplecoupon.distribution.dao.mapper.CouponTemplateMapper;
import com.fengxin.maplecoupon.distribution.dao.mapper.UserCouponMapper;
import com.fengxin.maplecoupon.distribution.mq.design.CouponTemplateDistributionEvent;
import com.fengxin.maplecoupon.distribution.mq.design.MessageWrapper;
import com.fengxin.maplecoupon.distribution.transation.ExecuteTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchExecutorException;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.*;

import static com.fengxin.maplecoupon.distribution.common.constant.DistributionRedisConstant.TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY;
import static com.fengxin.maplecoupon.distribution.common.constant.EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY;
import static com.fengxin.maplecoupon.distribution.common.constant.RocketMQConstant.*;

/**
 * @author FENGXIN
 * @date 2024/10/25
 * @project feng-coupon
 * @description 优惠券分发执行
 **/
@Slf4j(topic = "CouponExecuteDistributionConsumer")
@RequiredArgsConstructor
@Component
@RocketMQMessageListener (
        topic = COUPON_EXECUTE_DISTRIBUTION_TOPIC,
        consumerGroup = COUPON_EXECUTE_DISTRIBUTION_CONSUMER_GROUP
)
public class CouponExecuteDistributionConsumer implements RocketMQListener<MessageWrapper<CouponTemplateDistributionEvent>> {
    
    private final CouponTemplateMapper couponTemplateMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserCouponMapper userCouponMapper;
    private final CouponTaskFailMapper couponTaskFailMapper;
    private final ExecuteTransaction executeTransaction;
    private final CouponTaskMapper couponTaskMapper;
    
    private static final String BATCH_SAVE_USER_COUPON_LUA_PATH = "lua/batch_save_user_coupon.lua";
    private static final Integer BATCH_SAVE_USER_COUPON_SIZE = 100000;
    private final String excelPath = Paths.get("").toAbsolutePath() + "/tmp";
    @Override
    public void onMessage (MessageWrapper<CouponTemplateDistributionEvent> message) {
        log.info ("[消费者] 分发优惠券执行 消息体:{}",message.getMessage ());
        CouponTemplateDistributionEvent couponTemplateDistributionEvent = message.getMessage ();
        // 1. 如果消息体没有结束 且满足100000的整数倍
        if (BooleanUtil.isFalse (couponTemplateDistributionEvent.getDistributionEndFlag ())) {
            try {
                decrementCouponTemplateStockAndSaveUserCouponList(couponTemplateDistributionEvent);
            }catch (Exception e) {
                log.error("处理优惠券模板分发事件时发生异常: {}", e.getMessage(), e);
            }
            return;
        }
        // 2. 状态为true 说明Excel已经处理到最后一批
        if (couponTemplateDistributionEvent.getDistributionEndFlag ()){
            // 获取缓存的最后一批待发放用户集合大小
            String userSetKey = String.format (TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY , couponTemplateDistributionEvent.getCouponTaskId ());
            Long userSetSize = stringRedisTemplate.opsForSet ().size (userSetKey);
            if (ObjectUtil.isNotNull (userSetSize) && userSetSize > 0L){
                couponTemplateDistributionEvent.setBatchUserSetSize (userSetSize.intValue ());
                try {
                    decrementCouponTemplateStockAndSaveUserCouponList(couponTemplateDistributionEvent);
                } catch (Exception e) {
                    log.error ("扣减优惠券库存-保存用户优惠券发生异常: {}",e.getMessage ());
                }
                // 弹出所有缓存的待发放用户
                List<String> userIdAndRowNumList = stringRedisTemplate.opsForSet ().pop (userSetKey , Integer.MAX_VALUE);
                // 如果还有待发放用户 用户已领取优惠券 保存失败原因
                if (CollUtil.isNotEmpty (userIdAndRowNumList)){
                    List<CouponTaskFailDO> couponTaskFailDOList = new ArrayList<> ();
                    userIdAndRowNumList.forEach (each ->{
                        Map<Object, Object> failMap = MapUtil.builder ()
                                .put ("rowNum" , JSON.parseObject (each).getInteger ("rowNum"))
                                .put ("cause" , "用户已领取该优惠券")
                                .build ();
                        CouponTaskFailDO taskFailDO = CouponTaskFailDO.builder ()
                                .batchId (couponTemplateDistributionEvent.getCouponTaskBatchId ())
                                .jsonObject (JSON.toJSONString (failMap))
                                .build ();
                        couponTaskFailDOList.add (taskFailDO);
                    });
                    // 持久化
                    couponTaskFailMapper.insertBatch (couponTaskFailDOList);
                    // Redis优惠券库存恢复
                    String couponTemplateCacheKey = String.format(EngineRedisConstant.COUPON_TEMPLATE_KEY, couponTemplateDistributionEvent.getCouponTemplateId ());
                    stringRedisTemplate.opsForHash ().increment (couponTemplateCacheKey, "stock", userIdAndRowNumList.size ());
                }
            }
            // 将失败的记录放入Excel 这里应该上传云 OSS 或者 MinIO 等存储平台，但是增加部署成本就仅写入本地
            Long initId = 0L;
            String userCouponFailPath = excelPath + "/用户分发失败记录-" + couponTemplateDistributionEvent.getCouponTaskBatchId () +".xlsx";
            try (ExcelWriter excelWriter = EasyExcel.write(userCouponFailPath, CouponTaskFailExcelObject.class).build()) {
                WriteSheet writeSheet = EasyExcel.writerSheet("用户分发失败sheet").build();
                while (true){
                    List<CouponTaskFailDO> couponTaskFailList = getCouponTaskFailDOList (initId , couponTemplateDistributionEvent.getCouponTaskBatchId ());
                    // 首次循环，空集合时不创建无效的Excel空文件 不再进行后续逻辑
                    if (CollUtil.isEmpty (couponTaskFailList)){
                        userCouponFailPath = null;
                        break;
                    }
                    List<CouponTaskFailExcelObject> couponTaskFailExcelObjectList = couponTaskFailList.stream ()
                            .map (each -> JSON.parseObject (each.getJsonObject () , CouponTaskFailExcelObject.class))
                            .toList ();
                    excelWriter.write (couponTaskFailExcelObjectList, writeSheet);
                    // 最后一批数据 后面不再有数据 直接返回
                    if (couponTaskFailList.size () < BATCH_SAVE_USER_COUPON_SIZE){
                        break;
                    }
                    // 更新最大id
                    initId = couponTaskFailList.stream ()
                            .map (CouponTaskFailDO::getId)
                            .max (Comparator.naturalOrder ())
                            .orElse (initId);
                }
            }
            // 用户都已经接收到优惠券 设置结束时间
            CouponTaskDO couponTaskDO = CouponTaskDO.builder ()
                    .id (couponTemplateDistributionEvent.getCouponTaskId ())
                    .completionTime (new Date ())
                    .status (CouponTaskStatusEnum.SUCCESS.getStatus ())
                    .build ();
            couponTaskMapper.updateById (couponTaskDO);
        }
    }
    
    /**
     * 扣减券模板并保存用户优惠券列表
     *
     * @param couponTemplateDistributionEvent 优惠券模板分发事件
     */
    public void decrementCouponTemplateStockAndSaveUserCouponList(CouponTemplateDistributionEvent couponTemplateDistributionEvent) throws JsonProcessingException {
        // 1. 扣减数据库库存
        Integer decremented = decrementCouponTemplateStock (couponTemplateDistributionEvent , couponTemplateDistributionEvent.getBatchUserSetSize ());
        // 库存不足 扣减失败 直接返回
        if (decremented <= 0){
            log.error ("优惠券 {} 库存不足",couponTemplateDistributionEvent.getCouponTemplateId ());
            return;
        }
        // 2. 保存用户优惠券到数据库
        // 清除缓存的待发放用户
        String userSetKey = String.format (TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY , couponTemplateDistributionEvent.getCouponTaskId ());
        List<String> userIdAndRowNumList = stringRedisTemplate.opsForSet ().pop (userSetKey , decremented);
        // 用户优惠券集合
        if (CollUtil.isEmpty (userIdAndRowNumList)){
            log.info("Redis缓存待发放用户为空");
            return;
        }
        ArrayList<UserCouponDO> userCouponList = new ArrayList<> (userIdAndRowNumList.size ());
        Date now = new Date ();
        if (CollUtil.isNotEmpty (userIdAndRowNumList)) {
            // 添加用户优惠券实体到集合
            for (String each : userIdAndRowNumList) {
                JSONObject jsonObject = JSON.parseObject (each);
                // 优惠券有效期
                DateTime validityPeriod = DateUtil.offsetHour (now , JSON.parseObject (couponTemplateDistributionEvent.getCouponTemplateConsumeRule ()).getInteger ("validityPeriod"));
                UserCouponDO userCouponDO = UserCouponDO.builder ()
                        .id (IdUtil.getSnowflakeNextId ())
                        .couponTemplateId (couponTemplateDistributionEvent.getCouponTemplateId ())
                        .userId (jsonObject.getLong ("userId"))
                        .rowNum (jsonObject.getInteger ("rowNum"))
                        .receiveTime (now)
                        .receiveCount (1)
                        .source (CouponSourceEnum.PLATFORM.getType ())
                        .status (CouponStatusEnum.EFFECTIVE.getType ())
                        .validStartTime (now)
                        .validEndTime (validityPeriod)
                        .createTime (now)
                        .updateTime (now)
                        .delFlag (0)
                        .build ();
                userCouponList.add (userCouponDO);
            }
        }
        // 平台优惠券每个用户限领一次 批量新增用户优惠券记录，底层通过递归方式直到全部新增结束并记录失败日志
        batchSaveUserCouponList(couponTemplateDistributionEvent.getCouponTemplateId (),couponTemplateDistributionEvent.getCouponTaskBatchId (), userCouponList);
        // 将用户id 和 优惠券模板id-用户优惠券id 匹配放入缓存 记录用户优惠券
        List<String> userIdList = userCouponList.stream ()
                .map (UserCouponDO::getUserId)
                .map (String::valueOf)
                .toList ();
        String userIdListString = new ObjectMapper ().writeValueAsString (userIdList);
        List<String> couponIdAndCouUserIdList = userCouponList.stream ()
                .map (each ->
                     StrUtil.builder ()
                            .append (couponTemplateDistributionEvent.getCouponTemplateId ())
                            .append ("-")
                            .append (each.getId ())
                )
                .map (String::valueOf)
                .toList ();
        String couponIdAndCouUserIdString = new ObjectMapper ().writeValueAsString (couponIdAndCouUserIdList);
        List<String> key = List.of (USER_COUPON_TEMPLATE_LIST_KEY);
        List<String> args = List.of (userIdListString , couponIdAndCouUserIdString , String.valueOf (now.getTime ()));
        // 获取 LUA 脚本，并保存到 Hutool 的单例管理容器，下次直接获取不需要加载
        DefaultRedisScript<Void> buildLuaScript = Singleton.get(BATCH_SAVE_USER_COUPON_LUA_PATH, () -> {
            DefaultRedisScript<Void> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource (new ClassPathResource (BATCH_SAVE_USER_COUPON_LUA_PATH)));
            redisScript.setResultType(Void.class);
            return redisScript;
        });
        stringRedisTemplate.execute(buildLuaScript, key, args.toArray ());
    }
    
    /**
     * 扣减券模板
     *
     * @param couponTemplateDistributionEvent 优惠券模板分发事件
     * @param userSetSize                     用户优惠券数量
     * @return {@code Integer }
     */
    public Integer decrementCouponTemplateStock(CouponTemplateDistributionEvent couponTemplateDistributionEvent,Integer userSetSize) {
        // 扣减库存 防止多扣
        int decremented = couponTemplateMapper.decrementCouponTemplateStock (couponTemplateDistributionEvent.getShopNumber () , couponTemplateDistributionEvent.getCouponTemplateId () , userSetSize);
        // 如果扣减库存不足 则递归扣减完原来的优惠券数量
        if (!SqlHelper.retBool (decremented)){
            LambdaQueryWrapper<CouponTemplateDO> queryWrapper = new LambdaQueryWrapper<CouponTemplateDO> ()
                    .eq (CouponTemplateDO::getShopNumber, couponTemplateDistributionEvent.getShopNumber ())
                    .eq (CouponTemplateDO::getId, couponTemplateDistributionEvent.getCouponTemplateId ());
            CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNotNull (couponTemplateDO)){
                // 重试获取到可自减的库存数值 递归扣减完原来的库存
                return decrementCouponTemplateStock (couponTemplateDistributionEvent,couponTemplateDO.getStock ());
            }
        }
        return userSetSize;
    }
    
    /**
     * 批量保存用户优惠券列表
     *
     * @param couponTemplateId  优惠券模板 ID
     * @param couponTaskBatchId Coupon 任务批处理 ID
     * @param userCouponList 用户优惠券实体集合
     */
    private void batchSaveUserCouponList(Long couponTemplateId,Long couponTaskBatchId,List<UserCouponDO> userCouponList) {
        try {
            userCouponMapper.batchSaveUserCouponList (userCouponList);
        } catch (Exception e) {
            Throwable cause = e.getCause ();
            // 批量插入失败 用户优惠券唯一索引冲突 分发的优惠券仅允许用户领取一次
            if (cause instanceof BatchExecutorException){
                // 添加失败实体
                List<CouponTaskFailDO> couponTaskFailDOList = new ArrayList<> ();
                List<UserCouponDO> removeList = new ArrayList<> ();
                userCouponList.forEach (each -> {
                    try {
                        userCouponMapper.insert (each);
                    } catch (Exception ex) {
                        // 仅记录数据库存在的用户
                        if (executeTransaction.hasUserGetCoupon (each)){
                            MapBuilder<Object, Object> causeMap = MapUtil.builder ()
                                    .put ("rowNum" , each.getRowNum ())
                                    .put ("cause" , "用户已领取该优惠券");
                            CouponTaskFailDO couponTaskFailDO = CouponTaskFailDO.builder ()
                                    .batchId (couponTaskBatchId)
                                    .jsonObject (JSON.toJSONString (causeMap))
                                    .build ();
                            couponTaskFailDOList.add (couponTaskFailDO);
                            removeList.add (each);
                        }
                    }
                });
                // 持久化失败原因实体
                couponTaskFailMapper.insertBatch (couponTaskFailDOList);
                // 仅保留成功分发的用户
                userCouponList.removeAll (removeList);
                return;
            }
            log.error ("批量插入用户优惠券实体失败 error:{}", cause.getMessage());
            throw e;
        }
    }
    
    /**
     * 获取 Coupon 分发任务失败集合
     *
     * @param maxId             最大 ID
     * @param couponTaskBatchId Coupon 任务批处理 ID
     * @return {@code List<CouponTaskFailDO> }
     */
    public List<CouponTaskFailDO> getCouponTaskFailDOList (Long maxId,Long couponTaskBatchId) {
        // 书签记录解决深分页问题
        LambdaQueryWrapper<CouponTaskFailDO> queryWrapper = new LambdaQueryWrapper <CouponTaskFailDO> ()
                .eq (CouponTaskFailDO::getBatchId , couponTaskBatchId)
                .gt (CouponTaskFailDO::getId, maxId)
                .last ("LIMIT " + BATCH_SAVE_USER_COUPON_SIZE);
        return couponTaskFailMapper.selectList (queryWrapper);
    }
    
}
