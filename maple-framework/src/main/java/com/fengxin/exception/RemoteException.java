package com.fengxin.exception;

import com.fengxin.errorcode.BaseErrorCode;
import com.fengxin.errorcode.IErrorCode;

/**
 * @author FENGXIN
 * @date 2024/10/15
 * @project feng-coupon
 * @description 远程服务异常
 **/
public class RemoteException extends AbstractException {
    public RemoteException (IErrorCode errorCode) {
        this(null, null, errorCode);
    }
    
    public RemoteException (String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }
    
    public RemoteException (String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }
    
    public RemoteException (String message, Throwable throwable, IErrorCode errorCode) {
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
