package com.fish.chat.common.exception;

import com.fish.chat.common.enums.ErrorCodeEnum;
import lombok.Getter;

/**
 * 领域异常基类
 * 用于领域层业务规则校验失败时抛出
 */
@Getter
public class DomainException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误消息
     */
    private final String message;
    
    public DomainException(String message) {
        super(message);
        this.code = ErrorCodeEnum.DOMAIN_ERROR.getCode();
        this.message = message;
    }
    
    public DomainException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCodeEnum.DOMAIN_ERROR.getCode();
        this.message = message;
    }
}
