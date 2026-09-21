package com.archnova.common;

import lombok.Data;

/**
 * 统一响应
 */
@Data
public class R<T> {

    private int code;

    private String message;

    private T data;

    public static <T> R<T> ok(T data) {
        R<T> result = new R<>();
        result.code = 200;
        result.message = "ok";
        result.data = data;
        return result;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> result = new R<>();
        result.code = code;
        result.message = message;
        return result;
    }
}
