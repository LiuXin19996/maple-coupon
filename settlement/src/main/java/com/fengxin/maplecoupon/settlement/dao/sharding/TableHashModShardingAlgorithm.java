package com.fengxin.maplecoupon.settlement.dao.sharding;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.List;

/**
 * @author FENGXIN
 * @date 2024/10/17
 * @project feng-coupon
 * @description 沙丁自定义数据表分片算法 解决分片不均匀问题
 **/
public final class TableHashModShardingAlgorithm implements StandardShardingAlgorithm<Long> {
    
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        // 店铺编号
        long id = shardingValue.getValue();
        // 表数量
        int shardingCount = availableTargetNames.size();
        int mod = (int) hashShardingValue(id) % shardingCount;
        int index = 0;
        for (String targetName : availableTargetNames) {
            if (index == mod) {
                return targetName;
            }
            index++;
        }
        throw new IllegalArgumentException("No target found for value: " + id);
    }
    
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Long> shardingValue) {
        // 暂无范围分片场景，默认返回空
        return List.of();
    }
    
    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
}
