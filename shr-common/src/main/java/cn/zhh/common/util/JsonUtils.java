package cn.zhh.common.util;

import cn.zhh.common.enums.ErrorEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * JSON工具类
 *
 * @author Zhou Huanghua
 */
public class JsonUtils {
    private static ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 忽略json字符串的某些属性在pojo中不存在的情况，避免抛出异常
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorEnum.ERROR_TO_JSON, e);
        }
    }

    public static <T> T fromJson(String content, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(content, valueType);
        } catch (IOException e) {
            throw new BusinessException(ErrorEnum.ERROR_FROM_JSON, e);
        }
    }

    public static <T> T bytes2Pojo(byte[] content, Class<T> pojoClass) {
        if (Objects.isNull(content)) {
            throw new BusinessException(ErrorEnum.ERROR_FROM_JSON, "转化为对象时字节数组为空！");
        }
        try {
            return OBJECT_MAPPER.readValue(content, pojoClass);
        } catch (IOException e) {
            throw new BusinessException(ErrorEnum.ERROR_FROM_JSON, e);
        }
    }
}
