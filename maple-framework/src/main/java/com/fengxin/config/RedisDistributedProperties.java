package com.fengxin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author FENGXIN
 */
@Data
@ConfigurationProperties(prefix = RedisDistributedProperties.PREFIX)
public class RedisDistributedProperties {

    public static final String PREFIX = "framework.cache.redis";

    /**
     * Key 前缀
     */
    private String prefix;

    /**
     * Key 前缀字符集
     */
    private String prefixCharset = "UTF-8";
}
