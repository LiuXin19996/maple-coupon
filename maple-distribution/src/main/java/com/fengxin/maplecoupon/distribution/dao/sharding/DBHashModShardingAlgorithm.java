package com.fengxin.maplecoupon.distribution.dao.sharding;

import lombok.Getter;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @author FENGXIN
 * @date 2024/10/17
 * @project feng-coupon
 * @description 沙丁自定义数据库分片算法 解决分片不均匀问题
 **/
public final class DBHashModShardingAlgorithm implements StandardShardingAlgorithm<Long> {
    
    @Getter
    private Properties props;
    
    private int shardingCount;
    private static final String SHARDING_COUNT_KEY = "sharding-count";
    
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        // 店铺编号
        long id = shardingValue.getValue();
        // 数据库数量
        int dbSize = availableTargetNames.size();
        int mod = (int) hashShardingValue(id) % shardingCount / (shardingCount / dbSize);
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
    
    @Override
    public void init(Properties props) {
        this.props = props;
        shardingCount = getShardingCount(props);
    }
    
    private int getShardingCount(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(SHARDING_COUNT_KEY), () -> new ShardingAlgorithmInitializationException(getType(), "Sharding count cannot be null."));
        return Integer.parseInt(props.getProperty(SHARDING_COUNT_KEY));
    }
    
    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
}

