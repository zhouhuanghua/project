package cn.zhh.crawler.util;

import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Objects;

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

    public static void runIfNotEmpty(Collection collection, Runnable function) {
        if (CollectionUtils.isEmpty(collection)) {
            return;
        }
        function.run();
    }
}
