package com.biaddti.driver.opcda.util;

/**
 * 响应结果生成工具
 */
public class ResultGenerator {

    public static Result success() {
        return new Result()
                .setCode(ResultCode.SUCCESS.getCode())
                .setMessage(ResultCode.SUCCESS.getMessage());
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>()
                .setCode(ResultCode.SUCCESS.getCode())
                .setMessage(ResultCode.SUCCESS.getMessage())
                .setData(data);
    }

    public static Result success(String message) {
        return new Result()
                .setCode(ResultCode.SUCCESS.getCode())
                .setMessage(message);
    }
    public static Result fail() {
        return new Result()
                .setCode(ResultCode.INTERNAL_SERVER_ERROR.getCode())
                .setMessage(ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }
    public static Result fail(int code) {
        return new Result()
                .setCode(code)
                .setMessage(ResultCode.getMessage(code));
    }
    public static Result fail(int code, String message) {
        return new Result()
                .setCode(code)
                .setMessage(message);
    }
}
