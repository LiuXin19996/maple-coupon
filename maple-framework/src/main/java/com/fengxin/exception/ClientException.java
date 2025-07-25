package com.fengxin.exception;

import com.fengxin.errorcode.BaseErrorCode;
import com.fengxin.errorcode.IErrorCode;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project feng-coupon
 * @description 客户端异常
 **/
public class ClientException extends AbstractException{
    public ClientException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }
    
    public ClientException(String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }
    
    public ClientException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }
    
    public ClientException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }
    
    @Override
    public String toString() {
        return "ClientException{" +
                "code='" + errorCode + "'," +
                "message='" + errorMessage + "'" +
                '}';
    }
    
}
