package cn.zhh.crawler.chain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 对象包装器
 *
 * @author Zhou Huanghua
 * @date 2020/3/6 22:20
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ObjectWrapper<T> {

    private T obj;

    public static final ObjectWrapper create() {
        return new ObjectWrapper();
    }

    public static final <T> ObjectWrapper<T> create(T obj) {
        return new ObjectWrapper<>(obj);
    }

    public boolean isNull() {
        return Objects.isNull(obj);
    }

    public boolean nonNull() {
        return Objects.nonNull(obj);
    }
}
