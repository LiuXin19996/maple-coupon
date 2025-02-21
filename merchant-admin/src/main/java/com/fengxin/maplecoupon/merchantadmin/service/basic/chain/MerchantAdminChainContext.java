package com.fengxin.maplecoupon.merchantadmin.service.basic.chain;

import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author FENGXIN
 * @date 2024/10/16
 * @project feng-coupon
 * @description 商家后管责任链上下文容器
 * ApplicationContextAware 接口获取应用上下文，并复制局部变量方便后续使用；CommandLineRunner 项目启动后执行责任链容器的填充工作
 **/
@Component
public final class MerchantAdminChainContext<T> implements ApplicationContextAware, CommandLineRunner {
    private ApplicationContext applicationContext;
    // 存放责任链bean的容器
    private final Map<String, List<MerchantAdminAbstractChainHandler<T>>> abstractChainHandlerContainer = new ConcurrentHashMap<> ();
    
    public void handler(String mark, T requestParam) {
        List<MerchantAdminAbstractChainHandler<T>> handlers = abstractChainHandlerContainer.get(mark);
        if (CollUtil.isEmpty(handlers)) {
            throw new RuntimeException(String.format("[%s] Chain of Responsibility ID is undefined.", mark));
        }
        // 执行责任链逻辑 这里体现链的是order排序 根据排序进行对象处理
        handlers.forEach(each -> each.handler(requestParam));
    }
    
    @Override
    public void run (String... args) throws Exception {
        // 从Spring IOC获取对应的责任链bean 填充入container
        // 通过 applicationContext.getBeansOfType() 方法获取所有实现了 MerchantAdminAbstractChainHandler 的 bean。
        applicationContext.getBeansOfType(MerchantAdminAbstractChainHandler.class)
                .forEach((beanName, bean) -> {
            // 根据mark分组责任链
            // 判断 Mark 是否已经存在抽象责任链容器中，如果已经存在直接向集合新增；如果不存在，创建 Mark 和对应的责任链集合 每个mark对应一条责任链
            List<MerchantAdminAbstractChainHandler<T>> abstractChainHandlerContainerOrDefault = abstractChainHandlerContainer.getOrDefault (bean.mark () , new ArrayList<> ());
            abstractChainHandlerContainerOrDefault.add(bean);
            // 将处理器按 mark 进行归类，存入 abstractChainHandlerContainer
            abstractChainHandlerContainer.put (bean.mark (),abstractChainHandlerContainerOrDefault);
        });
        // 根据优先级排序
        abstractChainHandlerContainer.forEach((mark, unsortedChainHandlers) -> {
            // 对每个 Mark 对应的责任链实现类集合进行排序，优先级小的在前
            unsortedChainHandlers.sort(Comparator.comparing(Ordered::getOrder));
        });
    }
    
    @Override
    public void setApplicationContext (ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
