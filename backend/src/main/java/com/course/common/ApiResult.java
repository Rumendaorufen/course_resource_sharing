package com.course.common;

import lombok.Data;

@Data
public class ApiResult<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> ApiResult<T> error(String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> ApiResult<T> error(Integer code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> ApiResult<T> forbidden(String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(403);
        result.setMessage(message);
        return result;
    }
}
