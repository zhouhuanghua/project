package cn.zhh.admin.dto.rsp;

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
    private static final int SUCCESS = 0;

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
        return new Response<>(SUCCESS, null, (T)null);
    }

    public static <T> Response<T> ok(String msg) {
        return new Response<>(SUCCESS, msg, (T)null);
    }

    public static <T> Response<T> ok(T content) {
        return new Response<>(SUCCESS, null, content);
    }

    public static <T> Response<T> ok(String msg, T content) {
        return new Response<>(SUCCESS, msg, content);
    }

    public static <T> Response<T> err(int code, String msg) {
        return new Response<>(code, msg, (T)null);
    }

    public boolean isOk() {
        return Objects.equals(this.code, SUCCESS);
    }

    public boolean isErr() {
        return this.code != 0;
    }

    public T get() {
        if (isErr()) {
            throw new UnsupportedOperationException("result is error!");
        }
        return this.content;
    }
}
