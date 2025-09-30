package com.fish.chat.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.fish.chat.utils.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 全局异常捕获

    //    Sa-Token 权限异常捕获
    @ExceptionHandler(NotPermissionException.class)
    public Result handlerException(NotPermissionException e) {
        return Result.notPermission(e);
    }

    @ExceptionHandler(NotLoginException.class)
    public Result handlerException(NotLoginException e) {
        return Result.notLogin();
    }
}
