package com.fengxin.maplecoupon.engine.dao.sharding;

import cn.hutool.core.lang.Singleton;
import org.springframework.beans.factory.annotation.Value;

/**
 * dbsharding 实用程序
 *
 * @author fengxin
 * @date 2024-10-31
 */
public final class DBShardingUtil {
    /**
     * 数据库分片数量
     */
    private static Integer dbShardingCount;
    /**
     * 获取数据库分片算法类，在该类初始化时向 Singleton 放入实例
     */
    private static final DBHashModShardingAlgorithm DB_SHARDING_ALGORITHM = Singleton.get(DBHashModShardingAlgorithm.class);

    /**
     * 解决查询商家优惠券 IN 场景跨库表不存在问题
     *
     * @param shopNumber 分片键 shopNumber
     * @return 返回 shopNumber 所在的数据源
     */
    public static int doCouponSharding(Long shopNumber) {
        return DB_SHARDING_ALGORITHM.getShardingMod(shopNumber, dbShardingCount);
    }
    
    @Value("${one-coupon.db-config.db-count}")
    public void setDbShardingCount(Integer dbShardingCount) {
        DBShardingUtil.dbShardingCount = dbShardingCount;
    }
}