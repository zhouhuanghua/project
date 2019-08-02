package cn.zhh.common.enums;

import lombok.Getter;

/**
 * 错误枚举
 *
 * @author Zhou Huanghua
 */
public enum ErrorEnum {
    UN_KNOW(-1, "internal error"),
    INVALID_PARAM(-2, "invalid param"),
    DB_RECORD_NOT_EXIST(-3, "db record not exist"),
    DB_RECORD_NOT_UNIQUE(-4, "db record not unique"),
    DB_RECORD_NOT_RIGHT(-5, "db record not right"),
    DB_INSERT_FAIL(-6, "db insert fail"),
    DB_UPDATE_FAIL(-7, "db update fail"),
    ERROR_TO_JSON(-8, "error to json"),
    ERROR_FROM_JSON(-9, "error from json"),
    BAD_REQUEST(-10, "bad request")
    ;

    @Getter
    private int code;

    @Getter
    private String message;

    private static final int BASE_NUM = 10_000;

    private ErrorEnum(int code, String message) {
        this.code = BASE_NUM + code;
        this.message = message;
    }
}
