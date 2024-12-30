package com.fengxin.filter;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fengxin.common.GatewayErrorResult;
import com.fengxin.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * SpringCloud Gateway Token 拦截器
 * @author FENGXIN
 */
@Slf4j
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private final StringRedisTemplate stringRedisTemplate;

    public TokenValidateGatewayFilterFactory(StringRedisTemplate stringRedisTemplate) {
        super(Config.class);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter((exchange , chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getPath().toString();
            if (!isPathInWhiteList(requestPath, config.getWhitePathList ())) {
                String username = request.getHeaders().getFirst("username");
                String token = request.getHeaders().getFirst("token");
                Object userInfo;
                if (StringUtils.hasText(username) && StringUtils.hasText(token) && (userInfo = stringRedisTemplate.opsForHash().get("MapleCoupon-maple-coupon_auth:user:login:" + username, token)) != null) {
                    JSONObject userInfoJsonObject = JSON.parseObject(userInfo.toString());
                    String userId = userInfoJsonObject.getString ("id");
                    String shopNumber = Optional.ofNullable (userInfoJsonObject.getString ("shopNumber")).orElse("");
                    // if(!token.equals(userInfoJsonObject.getString("token"))){
                    //     return Mono.error(new RuntimeException("Token validation error"));
                    // }
                    if(!StringUtils.hasText (userId)){
                        return Mono.error(new RuntimeException("userId is null"));
                    }
                    if(!StringUtils.hasText (shopNumber)){
                        log.warn("shopNumber is null");
                    }
                    ServerWebExchange serverWebExchange = exchange.mutate().
                            request(b -> {
                                b.header ("userId",userId);
                                b.header ("shopNumber",shopNumber);
                            }).build();
                    // 用户校验成功后刷新时间 防止用户还在操作数据就退出登录
                    // 设置有效时间
                    stringRedisTemplate.expire ("MapleCoupon-maple-coupon_auth:user:login:" + username , 30L , TimeUnit.DAYS);
                    return chain.filter(serverWebExchange);
                }
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    GatewayErrorResult resultMessage = GatewayErrorResult.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("Token validation error")
                            .build();
                    return bufferFactory.wrap(JSON.toJSONString(resultMessage).getBytes());
                }));
            }
            return chain.filter(exchange);
        } ,100);
    }

    private boolean isPathInWhiteList(String requestPath, List<String> whitePathList) {
        return (!CollectionUtils.isEmpty(whitePathList) && whitePathList.stream().anyMatch(requestPath::startsWith));
    }
}