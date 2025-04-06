package com.jiahe.intellipichub.exception;
import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "Success"),
    PARAMS_ERROR(40000, "Invalid request parameters"),
    NOT_LOGIN_ERROR(40100, "Not logged in"),
    NO_AUTH_ERROR(40101, "No permission"),
    NOT_FOUND_ERROR(40400, "Requested data not found"),
    FORBIDDEN_ERROR(40300, "Access forbidden"),
    SYSTEM_ERROR(50000, "System internal exception"),
    OPERATION_ERROR(50001, "Operation failed");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}


