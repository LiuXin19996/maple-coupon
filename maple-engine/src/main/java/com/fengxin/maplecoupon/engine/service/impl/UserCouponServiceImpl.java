package com.fengxin.maplecoupon.engine.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.fengxin.exception.ClientException;
import com.fengxin.exception.ServiceException;
import com.fengxin.maplecoupon.engine.common.constant.EngineRedisConstant;
import com.fengxin.maplecoupon.engine.common.context.UserContext;
import com.fengxin.maplecoupon.engine.common.enums.RedisStockDecrementErrorEnum;
import com.fengxin.maplecoupon.engine.common.enums.UserCouponStatusEnum;
import com.fengxin.maplecoupon.engine.dao.entity.CouponSettlementDO;
import com.fengxin.maplecoupon.engine.dao.entity.CouponTemplateDO;
import com.fengxin.maplecoupon.engine.dao.entity.CouponTemplateRemindDO;
import com.fengxin.maplecoupon.engine.dao.entity.UserCouponDO;
import com.fengxin.maplecoupon.engine.dao.mapper.CouponSettlementMapper;
import com.fengxin.maplecoupon.engine.dao.mapper.CouponTemplateMapper;
import com.fengxin.maplecoupon.engine.dao.mapper.CouponTemplateRemindMapper;
import com.fengxin.maplecoupon.engine.dao.mapper.UserCouponMapper;
import com.fengxin.maplecoupon.engine.dto.req.*;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.maplecoupon.engine.dto.resp.CouponTemplateRemindQueryRespDTO;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponDelayCloseEvent;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponRedeemEvent;
import com.fengxin.maplecoupon.engine.mq.design.UserCouponRemindEvent;
import com.fengxin.maplecoupon.engine.mq.producer.UserCouponDelayCloseProducer;
import com.fengxin.maplecoupon.engine.mq.producer.UserCouponRedeemProducer;
import com.fengxin.maplecoupon.engine.mq.producer.UserCouponRemindProducer;
import com.fengxin.maplecoupon.engine.service.CouponTemplateService;
import com.fengxin.maplecoupon.engine.service.UserCouponService;
import com.fengxin.maplecoupon.engine.service.handler.dto.CouponTemplateRemindDTO;
import com.fengxin.maplecoupon.engine.util.SetUserCouponTemplateRemindTimeUtil;
import com.fengxin.maplecoupon.engine.util.StockDecrementReturnCombinedUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.fengxin.maplecoupon.engine.common.constant.EngineRedisConstant.*;

/**
 * @author FENGXIN
 * @date 2024/10/28
 * @project feng-coupon
 * @description 用户优惠券业务实现
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {
    private final CouponTemplateService couponTemplateService;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserCouponRedeemProducer userCouponRedeemProducer;
    private final CouponTemplateRemindMapper couponTemplateRemindDOMapper;
    private final UserCouponRemindProducer userCouponRemindProducer;
    private final RBloomFilter<String> cancelRemindBloomFilter;
    private final RedissonClient redissonClient;
    private final CouponTemplateMapper couponTemplateMapper;
    private static final String STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_PATH = "lua/stock_decrement_and_save_user_receive.lua";
    private final CouponSettlementMapper couponSettlementMapper;
    private final UserCouponMapper userCouponMapper;
    private final TransactionTemplate transactionTemplate;
    private final UserCouponDelayCloseProducer userCouponDelayCloseProducer;
    
    @Override
    public void redeemUserCouponv2 (CouponTemplateRedeemReqDTO requestParam) {
        // 校验优惠券是否存在缓存 存在数据 且在有效期
        CouponTemplateQueryRespDTO couponTemplateById = couponTemplateService.findCouponTemplateById (BeanUtil.toBean (requestParam , CouponTemplateQueryReqDTO.class));
        if (ObjectUtil.isNull (couponTemplateById)){
            throw new ServiceException ("优惠券 " + requestParam.getCouponTemplateId () + " 不存在" );
        }
        Date now = new Date ();
        boolean isIn = DateUtil.isIn (now , couponTemplateById.getValidStartTime () , couponTemplateById.getValidEndTime ());
        if (BooleanUtil.isFalse (isIn)){
            // 可认为是客户端恶意攻击
            throw new ServiceException ("兑换目标优惠券不在有效期范围");
        }
        // 校验通过 扣减缓存的优惠券库存 并验证用户是否超额领取优惠券
        // 将lua脚本放入Hutool容器
        DefaultRedisScript<Long> defaultRedisScript = Singleton.get (STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_PATH , () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<> ();
            redisScript.setScriptSource (new ResourceScriptSource (new ClassPathResource (STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_PATH)));
            redisScript.setResultType (Long.class);
            return redisScript;
        });
        // 领取上限
        JSONObject receiveRule = JSON.parseObject (couponTemplateById.getReceiveRule ());
        String limitPerPerson = receiveRule.getString ("limitPerPerson");
        String couponTemplateKey = String.format (COUPON_TEMPLATE_KEY , requestParam.getCouponTemplateId ());
        String userCouponTemplateKey = String.format (USER_COUPON_TEMPLATE_LIMIT_KEY , UserContext.getUserId () , requestParam.getCouponTemplateId ());
        long executeResult = stringRedisTemplate.execute (
                defaultRedisScript ,
                List.of (couponTemplateKey , userCouponTemplateKey) ,
                String.valueOf (couponTemplateById.getValidEndTime ().getTime ()) ,
                limitPerPerson
        );
        // 处理执行返回失败结果
        // 0 代表请求成功 1 代表优惠券已被领取完 2 代表用户已经达到领取上限
        // 用户领取次数 初始化为 0，每次领取成功后自增加 1
        long firstField = StockDecrementReturnCombinedUtil.extractFirstField (executeResult);
        if (RedisStockDecrementErrorEnum.isFail (firstField)){
            throw new ServiceException (RedisStockDecrementErrorEnum.fromType (firstField));
        }
        long secondField = StockDecrementReturnCombinedUtil.extractSecondField (executeResult);
        UserCouponRedeemEvent userCouponRedeemEvent = UserCouponRedeemEvent.builder ()
                .requestParam (requestParam)
                .couponTemplate (couponTemplateById)
                .receiveCount (Long.valueOf (secondField).intValue ())
                .userId (UserContext.getUserId ())
                .now (now)
                .build ();
        userCouponRedeemProducer.sendMessage (userCouponRedeemEvent);
    }
    
    @Override
    public void redeemUserCouponv1 (CouponTemplateRedeemReqDTO requestParam) {
        // 校验优惠券是否存在缓存 存在数据 且在有效期
        CouponTemplateQueryRespDTO couponTemplateById = couponTemplateService.findCouponTemplateById (BeanUtil.toBean (requestParam , CouponTemplateQueryReqDTO.class));
        if (ObjectUtil.isNull (couponTemplateById)){
            throw new ServiceException ("兑换目标优惠券不存在" + requestParam.toString ());
        }
        Date now = new Date ();
        boolean isIn = DateUtil.isIn (now , couponTemplateById.getValidStartTime () , couponTemplateById.getValidEndTime ());
        if (BooleanUtil.isFalse (isIn)){
            // 可认为是客户端恶意攻击
            throw new ServiceException ("兑换目标优惠券不在有效期范围");
        }
        // 校验通过 扣减缓存的优惠券库存 并验证用户是否超额领取优惠券
        // 将lua脚本放入Hutool容器
        DefaultRedisScript<Long> defaultRedisScript = Singleton.get (STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_PATH , () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<> ();
            redisScript.setScriptSource (new ResourceScriptSource (new ClassPathResource (STOCK_DECREMENT_AND_SAVE_USER_RECEIVE_PATH)));
            redisScript.setResultType (Long.class);
            return redisScript;
        });
        // 领取上限
        JSONObject receiveRule = JSON.parseObject (couponTemplateById.getReceiveRule ());
        String limitPerPerson = receiveRule.getString ("limitPerPerson");
        String couponTemplateKey = String.format (COUPON_TEMPLATE_KEY , requestParam.getCouponTemplateId ());
        String userCouponTemplateKey = String.format (USER_COUPON_TEMPLATE_LIMIT_KEY , UserContext.getUserId () , requestParam.getCouponTemplateId ());
        long executeResult = stringRedisTemplate.execute (
                defaultRedisScript ,
                List.of (couponTemplateKey , userCouponTemplateKey) ,
                String.valueOf (couponTemplateById.getValidEndTime ().getTime ()) ,
                limitPerPerson
        );
        // 处理执行返回失败结果
        // 0 代表请求成功 1 代表优惠券已被领取完 2 代表用户已经达到领取上限
        // 用户领取次数 初始化为 0，每次领取成功后自增加 1
        long firstField = StockDecrementReturnCombinedUtil.extractFirstField (executeResult);
        if (RedisStockDecrementErrorEnum.isFail (firstField)){
            throw new ServiceException (RedisStockDecrementErrorEnum.fromType (firstField));
        }
        long secondField = StockDecrementReturnCombinedUtil.extractSecondField (executeResult);
        // 使用编程式事务 处理优惠券扣减和新增用户优惠券
        transactionTemplate.executeWithoutResult (status ->{
            try {
                int decremented = couponTemplateMapper.decrementCouponTemplateStock (
                        Long.parseLong (requestParam.getShopNumber ())
                        , Long.parseLong (requestParam.getCouponTemplateId ())
                        , 1);
                if (!SqlHelper.retBool (decremented)){
                    throw new ServiceException ("您慢了一点点哦 优惠券被抢光啦");
                }
                // 新增用户优惠券
                DateTime validityPeriod = DateUtil.offsetHour (now , JSON.parseObject (couponTemplateById.getConsumeRule ()).getInteger ("validityPeriod"));
                UserCouponDO userCouponDO = UserCouponDO.builder ()
                        .couponTemplateId (Long.parseLong (requestParam.getCouponTemplateId ()))
                        .userId (Long.parseLong (UserContext.getUserId ()))
                        .source (requestParam.getSource ())
                        .receiveCount ((int) secondField)
                        .receiveTime (now)
                        .status (UserCouponStatusEnum.UNUSED.getCode ())
                        .validStartTime (now)
                        .validStartTime (couponTemplateById.getValidEndTime ())
                        .build ();
                userCouponMapper.insert (userCouponDO);
                // 添加用户领取优惠券模板缓存记录
                String userCouponListCacheKey = String.format(EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY, UserContext.getUserId());
                String userCouponItemCacheKey = StrUtil.builder()
                        .append(requestParam.getCouponTemplateId())
                        .append("_")
                        .append(userCouponDO.getId())
                        .toString();
                stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
                // 防止Redis宕机 造成数据丢失
                Double score;
                try {
                    score = stringRedisTemplate.opsForZSet ().score (userCouponListCacheKey,userCouponItemCacheKey);
                    // 插入失败 再插入一次
                    if (ObjectUtil.isNull (score)){
                        // 如果这里还是失败 做不到万无一失 只能增加成功的概率
                        stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
                    }
                }catch (Throwable e){
                    log.warn("查询Redis用户优惠券记录为空或抛异常，可能Redis宕机或主从复制数据丢失，基础错误信息：{}", e.getMessage());
                    // 如果直接抛异常大概率 Redis 宕机了，所以应该写个延时队列向 Redis 重试放入值。为了避免代码复杂性，这里直接写新增，大家知道最优解决方案即可
                    stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
                }
                // 发送延时消息 结束用户优惠券
                UserCouponDelayCloseEvent userCouponDelayCloseEvent = UserCouponDelayCloseEvent.builder ()
                        .userCouponId (userCouponDO.getId ())
                        .couponTemplateId (Long.valueOf (requestParam.getCouponTemplateId ()))
                        .userId (userCouponDO.getUserId ())
                        .delayDateTime (validityPeriod.getTime ())
                        .build ();
                SendResult sendResult = userCouponDelayCloseProducer.sendMessage (userCouponDelayCloseEvent);
                // 发送消息失败解决方案简单且高效的逻辑之一：打印日志并报警，通过日志搜集并重新投递
                if (ObjectUtil.notEqual(sendResult.getSendStatus().name(), "SEND_OK")) {
                    log.warn("发送优惠券关闭延时队列失败，消息参数：{}", JSON.toJSONString(userCouponDelayCloseEvent));
                }
            } catch (Throwable ex) {
                status.setRollbackOnly();
                // 优惠券已被领取完业务异常
                if (ex instanceof ServiceException) {
                    throw (ServiceException) ex;
                }
                if (ex instanceof DuplicateKeyException) {
                    log.error("用户重复领取优惠券，用户ID：{}，优惠券模板ID：{}", UserContext.getUserId(), requestParam.getCouponTemplateId());
                    throw new ServiceException("用户重复领取优惠券");
                }
                throw new ServiceException("优惠券领取异常，请稍候再试");
            }
        });
    }
    
    @Override
    public void createCouponRemind (CouponTemplateRemindTimeReqDTO requestParam) {
        // 校验优惠券是否存在
        CouponTemplateQueryRespDTO couponTemplateById = couponTemplateService.findCouponTemplateById (BeanUtil.toBean (requestParam , CouponTemplateQueryReqDTO.class));
        if (ObjectUtil.isNull (couponTemplateById)){
            throw new ServiceException ("优惠券 " + requestParam.getCouponTemplateId () + " 不存在" );
        }
        if (couponTemplateById.getValidEndTime ().before (new Date ())){
            throw new ServiceException ("优惠券 " + requestParam.getCouponTemplateId () + " 已结束" );
        }
        if (couponTemplateById.getValidStartTime ().before (new Date ())){
            throw new ServiceException ("优惠券 " + requestParam.getCouponTemplateId () + " 已开始" );
        }
        
        // 查询提醒信息
        LambdaQueryWrapper<CouponTemplateRemindDO> remindTemplateQueryWrapper = new LambdaQueryWrapper<CouponTemplateRemindDO> ()
                .eq (CouponTemplateRemindDO::getCouponTemplateId,requestParam.getCouponTemplateId ())
                .eq (CouponTemplateRemindDO::getShopNumber,requestParam.getShopNumber ());
        CouponTemplateRemindDO couponTemplateRemindDO = couponTemplateRemindDOMapper.selectOne (remindTemplateQueryWrapper);
        // 无提醒 创建提醒
        if (ObjectUtil.isNull (couponTemplateRemindDO)){
            CouponTemplateRemindDO templateRemindDO = CouponTemplateRemindDO.builder ()
                    .couponTemplateId (Long.valueOf (requestParam.getCouponTemplateId ()))
                    .shopNumber (Long.valueOf (requestParam.getShopNumber ()))
                    .information (SetUserCouponTemplateRemindTimeUtil.calculateRemindTime (requestParam.getRemindTime () , requestParam.getType ()))
                    .startTime (couponTemplateById.getValidStartTime ())
                    .userId (Long.parseLong (UserContext.getUserId ()))
                    .build ();
            couponTemplateRemindDOMapper.insert (templateRemindDO);
        }// 有提醒 更新提醒时间
        else {
            Long information = couponTemplateRemindDO.getInformation ();
            Long remindTime = SetUserCouponTemplateRemindTimeUtil.calculateRemindTime (requestParam.getRemindTime () , requestParam.getType ());
            if ((information & remindTime) != 0L){
                throw new ClientException ("该提醒时间已经有啦~");
            }
            couponTemplateRemindDO.setInformation (remindTime ^ information);
            couponTemplateRemindDOMapper.update (couponTemplateRemindDO,remindTemplateQueryWrapper);
        }
        
        // MQ发送任意延时信息 提醒用户
        UserCouponRemindEvent userCouponRemindEvent = UserCouponRemindEvent.builder ()
                .couponTemplateId (requestParam.getCouponTemplateId ())
                .shopNumber (requestParam.getShopNumber ())
                .userId (UserContext.getUserId ())
                .contact (UserContext.getUserId ())
                .type (requestParam.getType ())
                .remindTime (requestParam.getRemindTime ())
                .startTime (couponTemplateById.getValidStartTime ())
                .delayTime (DateUtil.offsetMinute (couponTemplateById.getValidStartTime () , -requestParam.getRemindTime ()).getTime ())
                .build ();
        userCouponRemindProducer.sendMessage (userCouponRemindEvent);
    }
    
    @Override
    public void cancelCouponRemind (CouponTemplateRemindCancelReqDTO requestParam) {
        // 校验优惠券模板是否存在
        CouponTemplateQueryRespDTO couponTemplateById = couponTemplateService.findCouponTemplateById (new CouponTemplateQueryReqDTO(
                requestParam.getShopNumber (),
                requestParam.getCouponTemplateId ()
        ));
        if (ObjectUtil.isNull (couponTemplateById)){
            throw new ServiceException ("优惠券不存在" + requestParam.getCouponTemplateId ());
        }
        // 模板存在
        if (couponTemplateById.getValidStartTime ().before (new Date ())){
            throw new ClientException ("无法取消已经开始的优惠券");
        }
        // 查询预约模板是否存在
        LambdaQueryWrapper<CouponTemplateRemindDO> couponTemplateRemindLambdaQueryWrapper = new LambdaQueryWrapper<CouponTemplateRemindDO> ()
                .eq (CouponTemplateRemindDO::getShopNumber,requestParam.getShopNumber ())
                .eq (CouponTemplateRemindDO::getCouponTemplateId,requestParam.getCouponTemplateId ());
        CouponTemplateRemindDO couponTemplateRemindDO = couponTemplateRemindDOMapper.selectOne (couponTemplateRemindLambdaQueryWrapper);
        if (ObjectUtil.isNull (couponTemplateRemindDO)){
            throw new ClientException ("没有此预约信息");
        }
        Long information = couponTemplateRemindDO.getInformation ();
        // 计算bitmap
        Long canalRemindTime = SetUserCouponTemplateRemindTimeUtil.calculateRemindTime (
                requestParam.getRemindTime () ,
                requestParam.getType ());
        if ((information & canalRemindTime) == 0L){
            throw new ClientException ("您没有该时间段的预约提醒");
        }
        // 取消预约
        canalRemindTime ^= information;
        couponTemplateRemindLambdaQueryWrapper.eq (CouponTemplateRemindDO::getInformation,information);
        if (canalRemindTime == 0L){
            // 已经取消了全部提醒 删除提醒模板
            if (couponTemplateRemindDOMapper.delete (couponTemplateRemindLambdaQueryWrapper) == 0){
                throw new ClientException ("取消预约提醒失败，请重试");
                // TODO 延时重试
            }
        }else {
            // 还有部分提醒 更新提醒模板
            couponTemplateRemindDO.setInformation (canalRemindTime);
            if (couponTemplateRemindDOMapper.update (couponTemplateRemindDO,couponTemplateRemindLambdaQueryWrapper) == 0){
                throw new ClientException ("取消预约提醒失败，请重试");
            }
        }
        // 移除取消的消息
        stringRedisTemplate.delete (String.format (USER_COUPON_REMIND_KEY , UserContext.getUserId ()));
        // 添加取消提醒到布隆过滤器
        cancelRemindBloomFilter.add (String.format (requestParam.getCouponTemplateId () , UserContext.getUserId () , requestParam.getRemindTime () , requestParam.getType ()));
    }
    
    @Override
    public boolean isCanalRemind(CouponTemplateRemindDTO requestParam){
        // 布隆过滤器取消提醒
        if (!cancelRemindBloomFilter.contains (String.format (requestParam.getCouponTemplateId () , UserContext.getUserId () , requestParam.getRemindTime () , requestParam.getType ()))){
            return false;
        }
        // 布隆过滤器存在 可能误判 仍然通过数据库查询
        LambdaQueryWrapper<CouponTemplateRemindDO> queryWrapper = new LambdaQueryWrapper<CouponTemplateRemindDO> ()
                .eq (CouponTemplateRemindDO::getCouponTemplateId,requestParam.getCouponTemplateId ())
                .eq (CouponTemplateRemindDO::getUserId,requestParam.getUserId ());
        CouponTemplateRemindDO couponTemplateRemindDO = couponTemplateRemindDOMapper.selectOne (queryWrapper);
        if (ObjectUtil.isNull (couponTemplateRemindDO)){
            return true;
        }
        // 取消了预约 只取消了该时间点
        Long canalRemindTime = SetUserCouponTemplateRemindTimeUtil.calculateRemindTime (requestParam.getRemindTime () , requestParam.getType ());
        return (canalRemindTime & couponTemplateRemindDO.getInformation ()) == 0L;
    }
    
    @Override
    public List<CouponTemplateRemindQueryRespDTO> listCouponRemind (CouponTemplateRemindQueryReqDTO requestParam) {
        // 查询缓存的预约信息
        String cacheRemind = stringRedisTemplate.opsForValue ().get (String.format (USER_COUPON_REMIND_KEY , requestParam.getUserId ()));
        if (StrUtil.isNotBlank (cacheRemind)){
            return JSON.parseArray (cacheRemind,CouponTemplateRemindQueryRespDTO.class);
        }
        // 查出预约信息
        List<CouponTemplateRemindQueryRespDTO> result = new ArrayList<> ();
        LambdaQueryWrapper<CouponTemplateRemindDO> queryWrapper = new LambdaQueryWrapper<CouponTemplateRemindDO> ()
                .eq (CouponTemplateRemindDO::getUserId,requestParam.getUserId ());
        List<CouponTemplateRemindDO> couponTemplateRemindList = couponTemplateRemindDOMapper.selectList (queryWrapper);
        if (CollUtil.isEmpty (couponTemplateRemindList)){
            return result;
        }
        // 根据优惠券id和店铺id查询优惠券信息 优惠券分库分表 整合返回
        List<Long> couponTemplateIdList = couponTemplateRemindList.stream ()
                .map (CouponTemplateRemindDO::getCouponTemplateId)
                .toList ();
        List<Long> shopNumberList = couponTemplateRemindList.stream ()
                .map (CouponTemplateRemindDO::getShopNumber)
                .toList ();
        List<CouponTemplateDO> couponTemplateDOList = couponTemplateService.listCouponTemplateByIdAndShopNumber (couponTemplateIdList , shopNumberList);
        result = BeanUtil.copyToList (couponTemplateDOList,CouponTemplateRemindQueryRespDTO.class);
        // 填充提醒时间和类型
        result.forEach (
                        each -> couponTemplateRemindList.stream ()
                        .filter (n ->
                                n.getCouponTemplateId ().toString ().equals (each.getId ()))
                        .findFirst ()
                        .ifPresent (n ->
                                SetUserCouponTemplateRemindTimeUtil.
                                fillRemindInformation (each,n.getInformation ()))
        );
        // 查询预约信息不会很频繁 用户只关心提醒动作 因此缓存时间设置1分钟 优化缓存压力
        stringRedisTemplate.opsForValue().set(String.format(USER_COUPON_REMIND_KEY, requestParam.getUserId()), JSON.toJSONString(result), 1, TimeUnit.MINUTES);
        return result;
    }
    
    @Override
    public void createPaymentRecord (CouponCreatePaymentReqDTO requestParam) {
        RLock lock = redissonClient.getLock (String.format (LOCK_COUPON_SETTLEMENT_KEY , requestParam.getCouponId ()));
        boolean tryLock = lock.tryLock ();
        if (BooleanUtil.isFalse (tryLock)){
            throw new ClientException ("当前优惠券正在核销或创建结算中......");
        }
        try {
            // 获取优惠券状态
            LambdaQueryWrapper<CouponSettlementDO> settlementQueryWrapper = new LambdaQueryWrapper<CouponSettlementDO> ()
                    .eq (CouponSettlementDO::getCouponId,requestParam.getCouponId ())
                    .eq (CouponSettlementDO::getUserId,Long.parseLong (UserContext.getUserId ()))
                    .in (CouponSettlementDO::getStatus,0,2);
            if (ObjectUtil.isNotNull (couponSettlementMapper.selectOne (settlementQueryWrapper))){
                throw new ClientException ("请检查优惠券使用状态");
            }
            // 获取用户优惠券状态
            LambdaQueryWrapper<UserCouponDO> userCouponQueryWrapper = new LambdaQueryWrapper<UserCouponDO> ()
                    .eq (UserCouponDO::getUserId,Long.parseLong (UserContext.getUserId ()))
                    .eq (UserCouponDO::getId,requestParam.getCouponId ());
            UserCouponDO userCouponDO = userCouponMapper.selectOne (userCouponQueryWrapper);
            if (ObjectUtil.isNull (userCouponDO)){
                throw new ClientException ("用户优惠券不存在");
            }
            if (userCouponDO.getValidEndTime ().before (new Date ())){
                throw new ClientException ("用户优惠券已过期");
            }
            if (userCouponDO.getStatus () != 0){
                throw new ClientException ("用户优惠券不可用");
            }
            // 查询优惠券消费规则
            CouponTemplateQueryRespDTO couponTemplateById = couponTemplateService.findCouponTemplateById (
                    new CouponTemplateQueryReqDTO (requestParam.getShopNumber (),userCouponDO.getCouponTemplateId ().toString ()));
            JSONObject consumeRuleDO = JSON.parseObject (couponTemplateById.getConsumeRule ());
            // 计算折扣金额
            BigDecimal discountAmount;
            if (couponTemplateById.getTarget ().equals (0)){
                // 商品专属券
                // 优惠券指定商品编号校验
                Optional<CouponCreatePaymentGoodsReqDTO> paymentGoodsDTO = requestParam.getGoodsList ().stream ()
                        .filter (each -> couponTemplateById.getGoods ().equals (each.getGoodsNumber ()))
                        .findFirst ();
                if (ObjectUtil.isNull (paymentGoodsDTO)){
                    throw new ClientException ("商品信息和优惠券描述不符");
                }
                // 计算折扣金额
                CouponCreatePaymentGoodsReqDTO couponCreatePaymentGoodsReqDTO = paymentGoodsDTO.get ();
                BigDecimal maximumDiscountAmount = consumeRuleDO.getBigDecimal ("maximumDiscountAmount");
                // 校验折扣后金额是否正确
                if (!couponCreatePaymentGoodsReqDTO.getGoodsAmount ().subtract (maximumDiscountAmount).equals (couponCreatePaymentGoodsReqDTO.getGoodsPayableAmount ())){
                    throw new ClientException ("商品折扣金额异常");
                }
                discountAmount = maximumDiscountAmount;
            }else {
                // 店铺通用券
                // 检查商品是否可用于当前店铺
                if (couponTemplateById.getSource () == 0 && !StrUtil.equals (requestParam.getShopNumber (),couponTemplateById.getShopNumber ())){
                    throw new ClientException ("该优惠券不可以在当前店铺使用哦~");
                }
                // 检查是否满足使用条件（满减 立减）
                BigDecimal termsOfUse = consumeRuleDO.getBigDecimal ("termsOfUse");
                if (requestParam.getOrderAmount ().compareTo (termsOfUse) < 0){
                    throw new ClientException ("当前商品未满足使用条件");
                }
                // 计算折扣金额
                BigDecimal maximumDiscountAmount = consumeRuleDO.getBigDecimal ("maximumDiscountAmount");
                switch (couponTemplateById.getType ()){
                    case 0:
                        // 立减券
                        discountAmount = maximumDiscountAmount;
                        break;
                    case 1:
                        // 满减券
                        discountAmount = maximumDiscountAmount;
                        break;
                    case 2:
                        // 折扣券
                        BigDecimal discountRate = consumeRuleDO.getBigDecimal ("discountRate");
                        discountAmount = requestParam.getOrderAmount ().multiply (discountRate);
                        // 如果大于了最大折扣金额
                        if (discountAmount.compareTo (maximumDiscountAmount) >= 0){
                            discountAmount = maximumDiscountAmount;
                        }
                        break;
                    default:
                        throw new ClientException ("无效的优惠券类型");
                }
            }
            // 计算折扣后的金额
            BigDecimal actualPaymentAmount = requestParam.getOrderAmount ().subtract (discountAmount);
            if (actualPaymentAmount.compareTo (requestParam.getPayableAmount ()) != 0){
                throw new ClientException ("折扣后的金额不一致");
            }
            // 创建优惠券结算单 并更新用户优惠券状态为锁定中
            transactionTemplate.executeWithoutResult (status ->{
                try {
                    CouponSettlementDO couponSettlementDO = CouponSettlementDO.builder ()
                            .couponId (requestParam.getCouponId ())
                            .userId (Long.parseLong (UserContext.getUserId ()))
                            .orderId (requestParam.getOrderId ())
                            .status (0)
                            .build ();
                    couponSettlementMapper.insert (couponSettlementDO);
                    LambdaUpdateWrapper<UserCouponDO> userCouponUpdateWrapper = new LambdaUpdateWrapper<UserCouponDO> ()
                            .eq (UserCouponDO::getId,requestParam.getCouponId ())
                            .eq (UserCouponDO::getUserId,Long.parseLong (UserContext.getUserId ()))
                            .eq (UserCouponDO::getStatus, UserCouponStatusEnum.UNUSED.getCode ());
                    UserCouponDO userCouponUpdateDO = UserCouponDO.builder ()
                            .status (UserCouponStatusEnum.LOCKING.getCode ())
                            .build ();
                    userCouponMapper.update (userCouponUpdateDO,userCouponUpdateWrapper);
                } catch (Throwable e) {
                    log.error("创建优惠券结算单失败", e);
                    status.setRollbackOnly();
                    throw e;
                }
            });
            // 将用户优惠券从缓存移除
            String userCouponListCacheKey = String.format(EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY, UserContext.getUserId());
            String userCouponItemCacheKey = StrUtil.builder()
                    .append(couponTemplateById.getId ())
                    .append("_")
                    .append(userCouponDO.getId())
                    .toString();
            stringRedisTemplate.opsForZSet().remove (userCouponListCacheKey,userCouponItemCacheKey);
        } finally {
            lock.unlock ();
        }
    }
    
    @Override
    public void processPayment (CouponProcessPaymentReqDTO requestParam) {
        RLock lock = redissonClient.getLock (String.format (LOCK_COUPON_SETTLEMENT_KEY , requestParam.getCouponId ()));
        boolean tryLock = lock.tryLock ();
        if (BooleanUtil.isFalse (tryLock)){
            throw new ClientException ("当前优惠券正在使用");
        }
        try {
            // 使用编程式事务 更新结算优惠券状态为已支付 将用户优惠券设置为已使用
            transactionTemplate.executeWithoutResult (status ->{
                try {
                    LambdaUpdateWrapper<CouponSettlementDO> settlementUpdateWrapper = new LambdaUpdateWrapper<CouponSettlementDO> ()
                            .eq (CouponSettlementDO::getCouponId,requestParam.getCouponId ())
                            .eq (CouponSettlementDO::getUserId,Long.parseLong (UserContext.getUserId ()))
                            .eq (CouponSettlementDO::getStatus,0);
                    CouponSettlementDO couponSettlementDO = CouponSettlementDO.builder ()
                            .status (2)
                            .build ();
                    int settlementUpdate = couponSettlementMapper.update (couponSettlementDO , settlementUpdateWrapper);
                    if (!SqlHelper.retBool (settlementUpdate)){
                        log.error ("优惠券结算已支付状态设置失败 用户优惠券id: {} 用户结算单id: {} 用户订单id: {}",
                                requestParam.getCouponId (),
                                couponSettlementDO.getId (),
                                couponSettlementDO.getOrderId ());
                        throw new ServiceException ("优惠券结算已支付状态设置失败");
                    }
                    LambdaUpdateWrapper<UserCouponDO> userCouponUpdateWrapper = new LambdaUpdateWrapper<UserCouponDO> ()
                            .eq (UserCouponDO::getId,requestParam.getCouponId ())
                            .eq (UserCouponDO::getUserId,Long.parseLong (UserContext.getUserId ()))
                            .eq (UserCouponDO::getStatus,UserCouponStatusEnum.LOCKING.getCode ());
                    UserCouponDO userCouponDO = UserCouponDO.builder ()
                            .status (UserCouponStatusEnum.USED.getCode ())
                            .build ();
                    int userCouponUpdate = userCouponMapper.update (userCouponDO , userCouponUpdateWrapper);
                    if (!SqlHelper.retBool (userCouponUpdate)){
                        log.error ("用户优惠券已使用状态设置失败 用户优惠券id: {} 用户结算单id: {} 用户订单id: {}",
                                requestParam.getCouponId (),
                                couponSettlementDO.getId (),
                                couponSettlementDO.getOrderId ());
                        throw new ServiceException ("用户优惠券已使用状态设置失败");
                    }
                } catch (Throwable e) {
                    log.error("核销优惠券结算单失败", e);
                    status.setRollbackOnly();
                    throw e;
                }
            });
        }finally {
            lock.unlock ();
        }
    }
    
    @Override
    public void processRefund (CouponProcessRefundReqDTO requestParam) {
        RLock lock = redissonClient.getLock (String.format (LOCK_COUPON_SETTLEMENT_KEY , requestParam.getCouponId ()));
        boolean tryLock = lock.tryLock ();
        if (BooleanUtil.isFalse (tryLock)){
            throw new ClientException ("当前优惠券正在使用");
        }
        try {
            // 使用编程式事务 更新结算优惠券状态为已退款 将用户优惠券设置为未使用
            transactionTemplate.executeWithoutResult (status -> {
                try{
                    LambdaUpdateWrapper<CouponSettlementDO> settlementUpdateWrapper = new LambdaUpdateWrapper<CouponSettlementDO> ()
                            .eq (CouponSettlementDO::getCouponId,requestParam.getCouponId ())
                            .eq (CouponSettlementDO::getUserId,Long.parseLong (UserContext.getUserId ()))
                            .eq (CouponSettlementDO::getStatus,2);
                    CouponSettlementDO couponSettlementDO = CouponSettlementDO.builder ()
                            .status (3)
                            .build ();
                    int settlementUpdate = couponSettlementMapper.update (couponSettlementDO , settlementUpdateWrapper);
                    if (!SqlHelper.retBool (settlementUpdate)){
                        log.error ("优惠券已退款状态设置失败 用户优惠券id: {} 用户结算单id: {} 用户订单id: {}",
                                requestParam.getCouponId (),
                                couponSettlementDO.getId (),
                                couponSettlementDO.getOrderId ());
                        throw new ServiceException ("优惠券已退款状态设置失败");
                    }
                    LambdaUpdateWrapper<UserCouponDO> userCouponUpdateWrapper = new LambdaUpdateWrapper<UserCouponDO> ()
                            .eq (UserCouponDO::getId,requestParam.getCouponId ())
                            .eq (UserCouponDO::getUserId,Long.parseLong (UserContext.getUserId ()))
                            .eq (UserCouponDO::getStatus,UserCouponStatusEnum.USED.getCode ());
                    UserCouponDO userCouponDO = UserCouponDO.builder ()
                            .status (UserCouponStatusEnum.UNUSED.getCode ())
                            .build ();
                    int userCouponUpdate = userCouponMapper.update (userCouponDO , userCouponUpdateWrapper);
                    if (!SqlHelper.retBool (userCouponUpdate)){
                        log.error ("用户优惠券未使用状态设置失败 用户优惠券id: {} 用户结算单id: {} 用户订单id: {}",
                                requestParam.getCouponId (),
                                couponSettlementDO.getId (),
                                couponSettlementDO.getOrderId ());
                        throw new ServiceException ("用户优惠券未使用状态设置失败");
                    }
                    // 将用户优惠券放入缓存 重新使用
                    LambdaQueryWrapper<UserCouponDO> userCouponQueryWrapper = new LambdaQueryWrapper<UserCouponDO> ()
                            .eq (UserCouponDO::getId,requestParam.getCouponId ())
                            .eq (UserCouponDO::getUserId,Long.parseLong (UserContext.getUserId ()));
                    UserCouponDO selectOne = userCouponMapper.selectOne (userCouponQueryWrapper);
                    String userCouponListCacheKey = String.format(EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY, UserContext.getUserId());
                    String userCouponItemCacheKey = StrUtil.builder()
                            .append(selectOne.getCouponTemplateId())
                            .append("_")
                            .append(selectOne.getId())
                            .toString();
                    stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, selectOne.getValidEndTime ().getTime());
                } catch (Throwable e) {
                    log.error("退款优惠券结算单失败", e);
                    status.setRollbackOnly();
                    throw e;
                }
            });
        }finally {
            lock.unlock ();
        }
    }
    
}
