package com.fengxin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fengxin.common.context.UserContext;
import com.fengxin.common.enums.CouponTaskSendTypeEnum;
import com.fengxin.common.enums.CouponTaskStatusEnum;
import com.fengxin.dao.entity.CouponTaskDO;
import com.fengxin.dao.mapper.CouponTaskMapper;
import com.fengxin.dto.req.CouponTaskCreateReqDTO;
import com.fengxin.dto.resp.CouponTemplateQueryRespDTO;
import com.fengxin.exception.ClientException;
import com.fengxin.service.CouponTaskService;
import com.fengxin.service.CouponTemplateService;
import com.fengxin.service.handler.excel.RowCountListener;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author FENGXIN
 * @date 2024/10/21
 * @project feng-coupon
 * @description 优惠券推送任务业务层
 **/
@Service
@RequiredArgsConstructor
public class CouponTaskServiceImpl extends ServiceImpl<CouponTaskMapper, CouponTaskDO> implements CouponTaskService {
    private final CouponTemplateService couponTemplateService;
    private final CouponTaskMapper couponTaskMapper;
    private final RedissonClient redissonClient;
    
    // 线程池
    ExecutorService threadPoolExecutor = new ThreadPoolExecutor (
            Runtime.getRuntime ().availableProcessors (),
            Runtime.getRuntime ().availableProcessors () << 1,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<> (),
            new ThreadPoolExecutor.DiscardPolicy ()
    );
    
    /**
     * 线程池异步执行获取Excel行数
     */
    public void refreshCouponTaskExcelRows (JSONObject jsonObject) {
        RowCountListener rowCountListener = new RowCountListener ();
        EasyExcel.read (jsonObject.getString ("fileAddress"),rowCountListener).sheet ().doRead ();
        int rowCount = rowCountListener.getRowCount ();
        
        // 刷新执行的行数
        CouponTaskDO couponTaskId = CouponTaskDO.builder ()
                .id (jsonObject.getLong ("couponTaskId"))
                .sendNum (rowCount)
                .build ();
        couponTaskMapper.updateById (couponTaskId);
    }
    @Override
    public void createCouponTask (CouponTaskCreateReqDTO requestParam) {
        // 验证参数非空
        // 验证参数格式正确
        // 验证参数依赖 比如选择定时发送，发送时间是否不为空等
        // 查询是否存在优惠券
        CouponTemplateQueryRespDTO couponTemplateById = couponTemplateService.findCouponTemplateById (requestParam.getCouponTemplateId ());
        if(ObjectUtil.isEmpty (couponTemplateById)){
            throw new ClientException ("优惠券模板不存在，请检查提交信息是否正确");
        }
        // ......
        
        // 构建优惠券推送任务数据库持久层实体
        CouponTaskDO couponTaskDO = BeanUtil.copyProperties (requestParam , CouponTaskDO.class);
        // 设置批次ID
        couponTaskDO.setBatchId (IdUtil.getSnowflakeNextId ());
        couponTaskDO.setOperatorId (Long.valueOf (UserContext.getUserId ()));
        couponTaskDO.setShopNumber (UserContext.getShopNumber ());
        // 设置发送类型
        couponTaskDO.setSendType (
                ObjectUtil.equals (requestParam.getSendType (), CouponTaskSendTypeEnum.IMMEDIATE.getType ())
                        // 立即执行
                        ? CouponTaskStatusEnum.IN_PROGRESS.getStatus ()
                        // 定时任务 待执行
                        : CouponTaskStatusEnum.PENDING.getStatus ()
        );
        
        couponTaskMapper.insert(couponTaskDO);
        
        // 刷新excel行数
        JSONObject delayJsonObject = new JSONObject ();
        delayJsonObject.put ("couponTaskId",couponTaskDO.getId ());
        delayJsonObject.put ("fileAddress",requestParam.getFileAddress ());
        // threadPoolExecutor.execute (()-> refreshCouponTaskExcelRows (delayJsonObject));
        
        // 防止应用宕机导致行数刷新失败 加一层延时队列兜底
        RBlockingDeque<Object> couponTaskSendNumDelayQueue = redissonClient.getBlockingDeque ("COUPON_TASK_SEND_NUM_DELAY_QUEUE");
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue (couponTaskSendNumDelayQueue);
        // 20s后数据理论已经刷新完
        delayedQueue.offer (delayJsonObject, 20, TimeUnit.SECONDS);
    }
}
