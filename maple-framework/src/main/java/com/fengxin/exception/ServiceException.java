package com.fengxin.exception;

import com.fengxin.errorcode.BaseErrorCode;
import com.fengxin.errorcode.IErrorCode;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project feng-coupon
 * @description 服务器异常
 **/
public class ServiceException extends AbstractException {
    public ServiceException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }
    
    public ServiceException(String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }
    
    public ServiceException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }
    
    public ServiceException(String message, Throwable throwable, IErrorCode errorCode) {
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
