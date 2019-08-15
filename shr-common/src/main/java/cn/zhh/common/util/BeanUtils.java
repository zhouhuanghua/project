package cn.zhh.common.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

public class BeanUtils {

    public static void copyProperties(Object source, Object target) throws BeansException {
        org.springframework.beans.BeanUtils.copyProperties(source, target);
    }

    public static void copyProperties(Object source, Object target, String... ignoreProperties) throws BeansException {
        org.springframework.beans.BeanUtils.copyProperties(source, target, ignoreProperties);
    }

    public static void copySelectedProperties(Object source, Object target, String... propertyNames) {
        for (String propertyName : propertyNames) {
            String[] words = propertyName.split(":");
            String sourcePropertyName = words[0];
            Object propertyValue = getPropertyValue(source, sourcePropertyName);
            int len = words.length;
            if (len == 1) {
                setPropertyValue(target, sourcePropertyName, propertyValue);
            } else if (len > 1) {
                for (int i = 1; i < len; i++) {
                    String targetPropertyName = words[i];
                    setPropertyValue(target, targetPropertyName, propertyValue);
                }
            }
        }
    }

    public static void deleteSelectedProperties(Object target, String... propertyNames) {
        for (String propertyName : propertyNames) {
            setPropertyValue(target, propertyName, null);
        }
    }

    public static <T> T createWithProperties(Object source, Class<T> targetClass, String... propertyNames) {
        try {
            T target = targetClass.newInstance();
            copyProperties(source, target, propertyNames);
            return target;
        } catch (Throwable ex) {
            throw new FatalBeanException(
                    "Could not new instance for class '" + targetClass.getName() + "'", ex);
        }
    }

    public static <T> List<T> createListWithProperties(List<?> sources, Class<T> targetClass) {
        List<T> result = new LinkedList<>();

        for (Object source : sources) {
            T target = createWithProperties(source, targetClass);
            result.add(target);
        }

        return result;
    }

    public static Object getPropertyValue(Object bean, String propertyName) {
        PropertyDescriptor propertyDescriptor = org.springframework.beans.BeanUtils.getPropertyDescriptor(bean.getClass(), propertyName);
        Method readMethod = propertyDescriptor.getReadMethod();
        if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
            readMethod.setAccessible(true);
        }
        return ReflectionUtils.invokeMethod(readMethod, bean);
    }

    public static void setPropertyValue(Object bean, String propertyName, Object propertyValue) {
        PropertyDescriptor propertyDescriptor = org.springframework.beans.BeanUtils.getPropertyDescriptor(bean.getClass(), propertyName);
        Method writeMethod = propertyDescriptor.getWriteMethod();
        if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
            writeMethod.setAccessible(true);
        }
        ReflectionUtils.invokeMethod(writeMethod, bean, propertyValue);
    }
}