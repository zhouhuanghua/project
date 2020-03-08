package cn.zhh.portal.util;

import cn.zhh.common.util.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 对象拷贝工具
 *
 * @author Zhou Huanghua
 */
@Slf4j
public class ObjCopyUtils {

    private ObjCopyUtils() {
        throw new UnsupportedOperationException("不支持创建实例！");
    }

    public static <T> T copyProperties(Object sourceObj, Class<T> targetClazz, boolean withSuperclass) throws IllegalAccessException, InstantiationException {
        return copyProperties(sourceObj, targetClazz.newInstance(), withSuperclass);
    }

    public static <T> T copyProperties(Object sourceObj, T targetObj, boolean withSuperclass) {
        List<Field> fieldList = new ArrayList<>();
        if (withSuperclass) {
            for (Class tempClass = targetObj.getClass(); Objects.nonNull(tempClass) && tempClass != Object.class;
                 tempClass = tempClass.getSuperclass()) {
                fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            }
        } else {
            fieldList.addAll(Arrays.asList(targetObj.getClass().getDeclaredFields()));
        }
        return copyProperties(sourceObj, targetObj, fieldList);
    }

    private static <T> T copyProperties(Object sourceObj, T targetObj, List<Field> targetObjFieldList) {
        for (Field targetObjField : targetObjFieldList) {
            Object value = sourceObj instanceof Map ? getSourceMapValue((Map) sourceObj, targetObjField.getName())
                    : getSourceObjValue(sourceObj, targetObjField.getName());
            targetObjField.setAccessible(true);
            try {
                targetObjField.set(targetObj, value);
            } catch (IllegalAccessException e) {
                continue;
            }
        }
        return targetObj;
    }

    private static Object getSourceMapValue(Map<String, Object> sourceMap, String name) {
        return sourceMap.get(name);
    }

    private static Object getSourceObjValue(Object sourceObj, String name) {
        Field field = null;
        for (Class clazz = sourceObj.getClass(); Objects.isNull(field) && clazz != Object.class;
             clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {}
        }
        if (Objects.isNull(field)) {
            return null;
        }
        field.setAccessible(true);
        try {
            return field.get(sourceObj);
        } catch (IllegalAccessException e) {
            log.error("获取字段属性值异常，e={}", ThrowableUtils.getStackTrace(e));
        }
        return null;
    }
}
