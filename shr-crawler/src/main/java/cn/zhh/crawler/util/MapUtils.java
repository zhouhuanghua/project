package cn.zhh.crawler.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Map工具类
 *
 * @author Zhou Huanghua
 */
public class MapUtils {

    /**
     * 构建一个Map，Key必须为String类型
     *
     * @param args 键值对：如("name", "张三", "age", "28")，一个name一个value依次填写
     * @return
     */
    public static <T> Map<String, T> buildMap(T... args) {
        if (Objects.isNull(args)) {
            return Collections.emptyMap();
        }
        Map<String, T> linkedHashMap = new LinkedHashMap<>(args.length / 2);
        for (int i = 0, end = args.length - 1; i < end; i += 2) {
            String name = (String) args[i];
            T value = args[i + 1];
            linkedHashMap.put(name, value);
        }
        return linkedHashMap;
    }
}
