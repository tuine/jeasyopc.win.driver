package com.biaddti.driver.opcda.util;

import java.util.Arrays;

/**
 * 响应码枚举，参考HTTP状态码的语义
 */
public enum ResultCode {
    SUCCESS(0, "Success"),//成功
    FAIL(400, "Bad Request"),//客户端请求错误
    UNAUTHORIZED(401, "Unauthorized"),//未认证（签名错误）
    NOT_FOUND(404, "Not Found"),//接口不存在
    FORBIDDEN(403, "Forbidden"),//接口没权限
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");//服务器内部错误

    private int code;

    private String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public static String getMessage(int code) {
        String message = "";
        for (ResultCode resultCode: ResultCode.values()) {
            if (resultCode.getCode() == code) {
                message = resultCode.message;
            }
        }
        return message;
    }
}
