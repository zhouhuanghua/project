package cn.zhh.crawler.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 函数工具类
 *
 * @author Zhou Huanghua
 */
public class FunctionUtils {

    public static void runIfNonNull(Object object, Runnable function) {
        if (Objects.nonNull(object)) {
            function.run();
        }
    }

    public static <T> void consumeIfNonNull(T t, Consumer<T> consumer ) {
        if (Objects.nonNull(t)) {
            consumer.accept(t);
        }
    }

    public static void runIfNotBlank(String str, Runnable function) {
        if (StringUtils.hasText(str)) {
            function.run();
        }
    }

    public static void consumeIfNotBlank(String str, Consumer<String> consumer) {
        if (StringUtils.hasText(str)) {
            consumer.accept(str);
        }
    }

    public static void runIfNotEmpty(Collection collection, Runnable function) {
        if (CollectionUtils.isEmpty(collection)) {
            return;
        }
        function.run();
    }

    public static <T> void consumeIfNotEmpty(Collection<T> collection, Consumer<Collection<T>> consumer) {
        if (CollectionUtils.isEmpty(collection)) {
            return;
        }
        consumer.accept(collection);
    }
}
