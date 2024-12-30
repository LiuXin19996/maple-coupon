package com.fengxin.maplecoupon.merchantadmin.common.context;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * @author FENGXIN
 */
@Component
@Slf4j
public class UserTransmitInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle (@Nullable HttpServletRequest request ,@Nullable HttpServletResponse response ,@Nullable Object handler) throws Exception {
        if (request != null) {
            String userName = Optional.ofNullable (request.getHeader ("username"))
                    .orElse ("");
            String userId =  Optional.ofNullable (request.getHeader ("userId"))
                    .orElse ("");
            Long shopNumber = Long.valueOf (Optional.ofNullable (request.getHeader ("shopNumber"))
                    .orElse("0"));
            UserInfoDTO userInfoDTO = new UserInfoDTO (userId, userName, shopNumber);
            UserContext.setUser(userInfoDTO);
            log.info ("userId: {} username: {} shopNumber: {}", userId, userName, shopNumber);
        }
        return true;
    }
    @Override
    public void afterCompletion(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler, Exception exception) throws Exception {
        UserContext.removeUser();
    }
}
