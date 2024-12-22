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
    private static final Integer BATCH_SAVE_USER_COUPON_SIZE = 5000;
    private final String excelPath = Paths.get("").toAbsolutePath() + "/tmp";
    @Override
    public void onMessage (MessageWrapper<CouponTemplateDistributionEvent> message) {
        log.info ("[消费者] 分发优惠券执行 消息体:{}",message.getMessage ());
        CouponTemplateDistributionEvent couponTemplateDistributionEvent = message.getMessage ();
        // 如果消息体没有结束 且满足5000的整数倍
        if (BooleanUtil.isFalse (couponTemplateDistributionEvent.getDistributionEndFlag ())) {
            try {
                decrementCouponTemplateStockAndSaveUserCouponList(couponTemplateDistributionEvent);
            } catch (JsonProcessingException e) {
                log.error ("JSON数据异常{}",e.getMessage ());
            }
            return;
        }
        // Excel状态为true 说明Excel已经处理完毕
        if (couponTemplateDistributionEvent.getDistributionEndFlag ()){
            // 获取缓存的待发放用户集合大小
            String userSetKey = String.format (TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY , couponTemplateDistributionEvent.getCouponTaskId ());
            Long userSetSize = stringRedisTemplate.opsForSet ().size (userSetKey);
            if (ObjectUtil.isNotNull (userSetSize) && userSetSize > 0L){
                couponTemplateDistributionEvent.setBatchUserSetSize (userSetSize.intValue ());
                try {
                    decrementCouponTemplateStockAndSaveUserCouponList(couponTemplateDistributionEvent);
                } catch (JsonProcessingException e) {
                    log.error ("JSON数据异常{}",e.getMessage ());
                }
                // 弹出所有缓存的待发放用户
                List<String> userIdAndRowNumList = stringRedisTemplate.opsForSet ().pop (userSetKey , Integer.MAX_VALUE);
                // 如果还有待发放用户 可能是库存不足 保存失败原因
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
                }
            }
            // 将失败的记录放入Excel 方便运维
            Long initId = 0L;
            boolean isFirstInit = true;
            String userCouponFailPath = excelPath + "/用户分发失败记录-" + couponTemplateDistributionEvent.getCouponTaskBatchId () +".xlsx";
            try (ExcelWriter excelWriter = EasyExcel.write(userCouponFailPath, CouponTaskFailExcelObject.class).build()) {
                // 这里注意 如果同一个sheet只要创建一次
                WriteSheet writeSheet = EasyExcel.writerSheet("用户分发失败sheet").build();
                while (true){
                    List<CouponTaskFailDO> couponTaskFailList = getCouponTaskFailDOList (initId , couponTemplateDistributionEvent.getCouponTaskBatchId ());
                    // 第一次且空集合时不创建无效的Excel空文件 以后的集合如果还是空继续跳过
                    if (CollUtil.isEmpty (couponTaskFailList)){
                        if (isFirstInit){
                            userCouponFailPath = null;
                        }
                        break;
                    }
                    isFirstInit = false;
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
        Integer decremented = decrementCouponTemplateStock (couponTemplateDistributionEvent , couponTemplateDistributionEvent.getBatchUserSetSize ());
        // 库存不足 扣减失败 直接返回
        if (decremented <= 0){
            return;
        }
        // 保存用户优惠券到数据库
        String userSetKey = String.format (TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY , couponTemplateDistributionEvent.getCouponTaskId ());
        // 清楚缓存的待发放用户
        List<String> userIdAndRowNumList = stringRedisTemplate.opsForSet ().pop (userSetKey , decremented);
        // 用户优惠券集合
        ArrayList<UserCouponDO> userCouponList = new ArrayList<> (userIdAndRowNumList.size ());
        Date now = new Date ();
        if (CollUtil.isNotEmpty (userIdAndRowNumList)) {
            // 添加用户优惠券实体到集合
            for (String each : userIdAndRowNumList) {
                JSONObject jsonObject = JSON.parseObject (each);
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
        // 平台优惠券每个用户限领一次。批量新增用户优惠券记录，底层通过递归方式直到全部新增成功
        batchSaveUserCouponList(couponTemplateDistributionEvent.getCouponTemplateId (),couponTemplateDistributionEvent.getCouponTaskBatchId (), userCouponList);
        // 将用户id和优惠券模板id匹配放入缓存
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
        // 使用CAS机制扣减库存
        int decremented = couponTemplateMapper.decrementCouponTemplateStock (couponTemplateDistributionEvent.getShopNumber () , couponTemplateDistributionEvent.getCouponTemplateId () , userSetSize);
        // 如果扣减库存不足 则扣减完原来的优惠券数量
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
            // 批量插入失败 唯一索引冲突
            if (cause instanceof BatchExecutorException){
                // 添加失败实体
                List<CouponTaskFailDO> couponTaskFailDOList = new ArrayList<> ();
                List<UserCouponDO> removeList = new ArrayList<> ();
                userCouponList.forEach (each -> {
                    try {
                        userCouponMapper.insert (each);
                    } catch (Exception ex) {
                        if (executeTransaction.hasUserGetCoupon (each)){
                            MapBuilder<Object, Object> causeMap = MapUtil.builder ()
                                    .put ("rowNum" , each.getRowNum ())
                                    .put ("cause" , "用户已获取该优惠券");
                            CouponTaskFailDO couponTaskFailDO = CouponTaskFailDO.builder ()
                                    .batchId (couponTaskBatchId)
                                    .jsonObject (JSON.toJSONString (causeMap))
                                    .build ();
                            couponTaskFailDOList.add (couponTaskFailDO);
                            removeList.add (each);
                        }
                    }
                });
                // 持久化失败原因
                couponTaskFailMapper.insertBatch (couponTaskFailDOList);
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
        LambdaQueryWrapper<CouponTaskFailDO> queryWrapper = new LambdaQueryWrapper <CouponTaskFailDO> ()
                .eq (CouponTaskFailDO::getBatchId , couponTaskBatchId)
                .gt (CouponTaskFailDO::getId, maxId)
                .last ("LIMIT " + BATCH_SAVE_USER_COUPON_SIZE);
        return couponTaskFailMapper.selectList (queryWrapper);
    }
    
}
