package com.fish.chat.common.exception;

import com.fish.chat.common.enums.ErrorCodeEnum;
import lombok.Getter;

/**
 * 应用异常基类
 * 用于应用层流程编排失败时抛出
 */
@Getter
public class AppException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误消息
     */
    private final String message;
    
    public AppException(String message) {
        super(message);
        this.code = ErrorCodeEnum.APP_ERROR.getCode();
        this.message = message;
    }
    
    public AppException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public AppException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCodeEnum.APP_ERROR.getCode();
        this.message = message;
    }
}
