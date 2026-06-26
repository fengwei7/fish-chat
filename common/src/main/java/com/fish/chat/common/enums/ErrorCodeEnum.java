package com.fish.chat.common.enums;

import com.fish.chat.common.exception.BusinessException;
import lombok.Getter;

/**
 * @Author: Arjen
 * @CreateTime: 2025-11-03
 * @Description: 异常值枚举
 * @Version: 1.0
 */

@Getter
public enum ErrorCodeEnum {
    /**
     * ========== 通用 ==========
     */
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    SERVER_ERROR(500, "系统繁忙，请稍后再试"),

    /**
     * ========== 通用业务 从1编号 ==========
     */
    COMMON_FAILED(1001, "通用失败"),
    DATA_NOT_EXIST(1002, "数据不存在"),
    DATA_EXIST(1003, "数据已存在"),
    OPERATION_FAILED(1004, "操作失败"),
    TOKEN_EXPIRED(1005, "登录过期，请重新登录"),
    LOGIN_FAILED(1006, "登录失败，请检查账号或密码"),
    REGISTER_FAILED(1007,"注册失败"),
        
    /**
     * ========== DDD 架构异常 ==========
     */
    DOMAIN_ERROR(1101, "领域业务规则校验失败"),
    APP_ERROR(1102, "应用层流程执行失败"),
    INFRASTRUCTURE_ERROR(1103, "基础设施层执行失败"),


    /**
     * ========== 具体业务异常 从2编号 =========
     */
    USER_INFO_EMPTY(2001,"用户信息不能为空"),
    USERNAME_EMPTY(2002,"用户名不能为空"),
    PASSWORD_EMPTY(2003,"密码不能为空"),
    PASSWORD_FORMAT_INVALID(2004,"密码格式不正确"),
    USERNAME_NOT_UNIQUE(2005,"该用户已存在"),


    /**
     * ========== 文件相关异常 从3编号 =========
     */
    FILE_EMPTY(3001,"上传文件不能为空"),
    FILE_TOO_LARGE(3002,"文件大小超过限制"),
    INVALID_FILENAME(3003,"文件名无效"),
    INVALID_FILETYPE(3004,"文件类型无效"),
    INVALID_FILE_EXTENSION(3005,"文件没有有效扩展名");



    private final Integer code;
    private final String message;

    ErrorCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    //这里直接获取异常枚举的code 和 message，封装成异常返回，可以用这个方法直接抛出异常
    public BusinessException toException() {
        return new BusinessException(this.code, this.message);
    }
}
