package com.fengxin.maplecoupon.engine.dao.sharding;

import cn.hutool.core.lang.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 用户db 分片算法
 *
 * @author fengxin
 * @date 2024-12-25
 */
public class DBUserHashModShardingAlgorithm implements StandardShardingAlgorithm<String> {
    @Getter
    private Properties props;
    
    private int shardingCount;
    private static final String SHARDING_COUNT_KEY = "sharding-count";
    
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        String username = shardingValue.getValue();
        int dbSize = availableTargetNames.size();
        int mod = (int) hashShardingValue(username) % shardingCount / (shardingCount / dbSize);
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
    
    @Override
    public void init(Properties props) {
        this.props = props;
        shardingCount = getShardingCount(props);
        Singleton.put(this);
    }
    
    private int getShardingCount(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(SHARDING_COUNT_KEY), () -> new ShardingAlgorithmInitializationException (getType(), "Sharding count cannot be null."));
        return Integer.parseInt(props.getProperty(SHARDING_COUNT_KEY));
    }
    
    public int getShardingMod(long id, int availableTargetSize) {
        return (int) hashShardingValue(id) % shardingCount / (shardingCount / availableTargetSize);
    }
    
    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }
}
