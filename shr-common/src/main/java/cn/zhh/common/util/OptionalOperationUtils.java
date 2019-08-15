package cn.zhh.common.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 可选操作工具类
 *
 * @author Zhou Huanghua
 */
public class OptionalOperationUtils {

    public static <T> void consumeIfNonNull(T t, Consumer<T> consumer) {
        if (Objects.nonNull(t)) {
            consumer.accept(t);
        }
    }

    public static void consumeIfNotBlank(String str, Consumer<String> consumer) {
        if (StringUtils.hasText(str)) {
            consumer.accept(str);
        }
    }

    public static <T> void consumeIfNotEmpty(Collection<T> collection, Consumer<Collection<T>> consumer) {
        if (CollectionUtils.isEmpty(collection)) {
            return;
        }
        consumer.accept(collection);
    }
}
