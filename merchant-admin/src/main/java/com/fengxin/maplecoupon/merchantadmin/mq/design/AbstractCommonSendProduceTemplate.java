package com.fengxin.maplecoupon.merchantadmin.mq.design;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;

/**
 * @author FENGXIN
 * @date 2024/10/22
 * @project feng-coupon
 * @description 模板方法模式 抽象类
 **/
@RequiredArgsConstructor
@Slf4j(topic = "CommonSendProduceTemplate")
public abstract class AbstractCommonSendProduceTemplate<T> {
    private final RocketMQTemplate rocketMQTemplate;
    
    /**
     * 构建消息相关参数
     * @param messageSendEvent 消息发送事件
     * @return 消息发送基础属性实体
     */
    protected abstract BaseSendExtendDTO buildBaseSendExtendParam(T messageSendEvent);
    
    /**
     * 构建消息体
     * @param messageSendEvent 消息发送事件
     * @param baseSendExtendDTO 消息发送基础属性实体
     * @return {@code Message<?> } 消息基本参数
     */
    protected abstract Message<?> buildMessage(T messageSendEvent, BaseSendExtendDTO baseSendExtendDTO);
    
    /**
     * 发送消息
     *
     * @param messageSendEvent message send 事件
     * @return {@code SendResult }
     */
    public SendResult sendMessage(T messageSendEvent) {
        BaseSendExtendDTO baseSendExtendDTO = buildBaseSendExtendParam (messageSendEvent);
        SendResult sendResult;
        try {
            // 构建目标落点 topic:tag
            StringBuilder destinationString = StrUtil.builder ().append (baseSendExtendDTO.getTopic ());
            if (StrUtil.isNotBlank (baseSendExtendDTO.getTag ())) {
                destinationString.append (baseSendExtendDTO.getTag ());
            }
            
            // 发送延迟消息
            if (ObjectUtil.isNotNull (baseSendExtendDTO.getDelayTime ())){
                sendResult = rocketMQTemplate.syncSendDeliverTimeMills (
                        destinationString.toString (),
                        buildMessage (messageSendEvent,baseSendExtendDTO),
                        baseSendExtendDTO.getDelayTime ()
                );
            }else {
                sendResult = rocketMQTemplate.syncSend (
                        destinationString.toString (),
                        buildMessage (messageSendEvent,baseSendExtendDTO),
                        baseSendExtendDTO.getSentTimeout ()
                );
            }
            log.info ("消息发送：[生产者]{} 发送结果{} 消息ID{} 消息key",baseSendExtendDTO.getEventName (),sendResult,baseSendExtendDTO.getTopic());
        }catch (Throwable e) {
            log.error ("消息发送失败 [生产者]{} 消息体{}",baseSendExtendDTO.getEventName (),messageSendEvent);
            throw e;
        }
        return sendResult;
    }
    
}
