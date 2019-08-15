package cn.zhh.admin.dto.rsp;

import cn.zhh.common.enums.ErrorEnum;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Objects;

/**
 * 通用结果返回对象
 *
 * @author z_hh
 */
@ToString
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 6547662806723050209L;

    private static final int SUCCESS = 1;

    private static final String OK = "OK";

    @Getter
    private Integer code;

    @Getter
    private String msg;

    @Getter
    private T content;

    private Response(Integer code, String msg, T content) {
        this.code = code;
        this.msg = msg;
        this.content = content;
    }

    public static <T> Response<T> ok() {
        return new Response<>(SUCCESS, OK, (T) null);
    }

    public static <T> Response<T> ok(String msg) {
        return new Response<>(SUCCESS, msg, (T) null);
    }

    public static <T> Response<T> ok(T content) {
        return new Response<>(SUCCESS, OK, content);
    }

    public static <T> Response<T> ok(String msg, T content) {
        return new Response<>(SUCCESS, msg, content);
    }

    public static <T> Response<T> err(ErrorEnum errorEnum, String msg) {
        return new Response<>(errorEnum.getCode(), msg, (T) null);
    }

    public static <T> Response<T> err(int code, String msg) {
        return new Response<>(code, msg, (T) null);
    }

    public boolean isOk() {
        return Objects.equals(this.code, SUCCESS);
    }

    public boolean isErr() {
        return this.code != SUCCESS;
    }

    public T get() {
        if (isErr()) {
            throw new UnsupportedOperationException("result is error!");
        }
        return this.content;
    }
}
