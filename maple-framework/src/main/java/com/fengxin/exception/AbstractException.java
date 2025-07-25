package com.fengxin.exception;

import com.fengxin.errorcode.IErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project feng-coupon
 * @description 抽象项目中三类异常体系，客户端异常、服务端异常以及远程服务调用异常
 **/
@Getter
public abstract class AbstractException extends RuntimeException {
    public final String errorCode;
    
    public final String errorMessage;
    
    public AbstractException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = Optional.ofNullable(StringUtils.hasLength(message) ? message : null).orElse(errorCode.message());
    }
}
