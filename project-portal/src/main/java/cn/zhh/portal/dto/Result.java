package cn.zhh.portal.dto;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

/**
 * 通用结果返回对象
 *
 * @author Zhou Huanghua
 */
@ToString
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 6547662806723050209L;
    private static final int SUCCESS = 200;
    private static final int ERROR = 500;

    @Getter
    private Integer code;
    @Getter
    private String msg;
    @Getter
    private T info;

    private Result(Integer code, String msg, T info) {
        this.code = code;
        this.msg = msg;
        this.info = info;
    }

    public static <T> Result<T> ok() {
        return new Result<>(SUCCESS, null, null);
    }

    public static <T> Result<T> ok(String msg) {
        return new Result<>(SUCCESS, msg, null);
    }

    public static <T> Result<T> ok(T info) {
        return new Result<>(SUCCESS, null, info);
    }

    public static <T> Result<T> ok(String msg, T info) {
        return new Result<>(SUCCESS, msg, info);
    }

    public static <T> Result<T> err() {
        return new Result<>(ERROR, null, (T)null);
    }

    public static <T> Result<T> err(String msg) {
        return new Result<>(ERROR, msg, (T)null);
    }

    public boolean isOk() {
        return Objects.equals(this.code, SUCCESS);
    }

    public boolean isErr() {
        return Objects.equals(this.code, ERROR);
    }

    public T get() {
        if (isErr()) {
            throw new UnsupportedOperationException("result is error!");
        }
        return this.info;
    }
}
