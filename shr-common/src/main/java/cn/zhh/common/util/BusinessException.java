package cn.zhh.common.util;

import cn.zhh.common.enums.ErrorEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 业务异常类
 *
 * @author Zhou Huanghua
 */
public class BusinessException extends RuntimeException {

    @Getter
    @Setter
    private int code;

    public BusinessException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
        this.code = errorEnum.getCode();
    }

    public BusinessException(ErrorEnum errorEnum, String message) {
        super(message);
        this.code = errorEnum.getCode();
    }

    public BusinessException(ErrorEnum errorEnum, Throwable cause) {
        super(errorEnum.getMessage(), cause);
        this.code = errorEnum.getCode();
    }

    public BusinessException(ErrorEnum errorEnum, String message, Throwable cause) {
        super(message, cause);
        this.code = errorEnum.getCode();
    }
}