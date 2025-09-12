package com.fish.chat.utils.result;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.util.SaFoxUtil;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 照抄 package cn.dev33.satoken.util.SaResult;
 */
public class Result extends LinkedHashMap<String, Object> implements Serializable {

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_ERROR = 500;
    public static final int CODE_NOT_PERMISSION = 403;
    public static final int CODE_NOT_LOGIN = 401;
    private static final long serialVersionUID = 1L;

    public Result() {
    }

    public Result(int code, String msg, Object data) {
        this.setCode(code);
        this.setMsg(msg);
        this.setData(data);
    }

    public Result(Map<String, ?> map) {
        this.setMap(map);
    }

    public static Result ok() {
        return new Result(200, "ok", null);
    }

    public static Result ok(String msg) {
        return new Result(200, msg, null);
    }

    public static Result code(int code) {
        return new Result(code, null, null);
    }

    public static Result data(Object data) {
        return new Result(200, "ok", data);
    }

    public static Result error() {
        return new Result(500, "error", null);
    }

    public static Result error(String msg) {
        return new Result(500, msg, null);
    }

    public static Result notLogin() {
        return new Result(401, "not login", null);
    }

    public static Result notPermission() {
        return new Result(403, "not permission", null);
    }

    public static Result get(int code, String msg, Object data) {
        return new Result(code, msg, data);
    }

    public static Result empty() {
        return new Result();
    }

    public Integer getCode() {
        return (Integer) this.get("code");
    }

    public Result setCode(int code) {
        this.put("code", code);
        return this;
    }

    public String getMsg() {
        return (String) this.get("msg");
    }

    public Result setMsg(String msg) {
        this.put("msg", msg);
        return this;
    }

    public Object getData() {
        return this.get("data");
    }

    public Result setData(Object data) {
        this.put("data", data);
        return this;
    }

    public Result set(String key, Object data) {
        this.put(key, data);
        return this;
    }

    public <T> T get(String key, Class<T> cs) {
        return SaFoxUtil.getValueByType(this.get(key), cs);
    }

    public Result setMap(Map<String, ?> map) {
        if (map != null) {
            Iterator var2 = map.keySet().iterator();

            while (var2.hasNext()) {
                String key = (String) var2.next();
                this.put(key, map.get(key));
            }
        }

        return this;
    }

    public Result setJsonString(String jsonString) {
        Map<String, Object> map = SaManager.getSaJsonTemplate().jsonToMap(jsonString);
        return this.setMap(map);
    }

    public Result removeDefaultFields() {
        this.remove("code");
        this.remove("msg");
        this.remove("data");
        return this;
    }

    public Result removeNonDefaultFields() {
        Iterator var1 = this.keySet().iterator();

        while (var1.hasNext()) {
            String key = (String) var1.next();
            if (!"code".equals(key) && !"msg".equals(key) && !"data".equals(key)) {
                this.remove(key);
            }
        }

        return this;
    }

    public String toString() {
        return "{\"code\": " + this.getCode() + ", \"msg\": " + this.transValue(this.getMsg()) + ", \"data\": "
            + this.transValue(this.getData()) + "}";
    }

    private String transValue(Object value) {
        if (value == null) {
            return null;
        } else {
            return value instanceof String ? "\"" + value + "\"" : String.valueOf(value);
        }
    }
}
