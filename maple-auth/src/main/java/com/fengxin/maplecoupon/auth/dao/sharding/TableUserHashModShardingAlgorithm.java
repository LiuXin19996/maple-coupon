package com.fengxin.maplecoupon.auth.dao.sharding;


import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.List;

/**
 * Table User Hash Mod 分片算法
 *
 * @author fengxin
 * @date 2024-12-25
 */
public class TableUserHashModShardingAlgorithm implements StandardShardingAlgorithm<String> {
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        // 用户名
        String username = shardingValue.getValue();
        // 表数量
        int shardingCount = availableTargetNames.size();
        int mod = (int) hashShardingValue(username) % shardingCount;
        int index = 0;
        for (String targetName : availableTargetNames) {
            if (index == mod) {
                return targetName;
            }
            index++;
        }
        throw new IllegalArgumentException("No target found for value: " + username);
    }
    
    @Override
    public Collection<String> doSharding (Collection<String> collection , RangeShardingValue<String> rangeShardingValue) {
        return List.of ();
    }
    
    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
}
