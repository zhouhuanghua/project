package cn.zhh.portal;

import cn.zhh.common.dto.PositionInfo;
import cn.zhh.portal.dto.PositionSearchVO;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 测试
 *
 * @author Zhou Huanghua
 */
public class MyTest {

    @Test
    public void testGetField() {
        Class targetClazz = PositionSearchVO.class;
        List<Field> fieldList = new ArrayList<>();
        for (Class tempClass = targetClazz; Objects.nonNull(tempClass) && PositionInfo.class.isAssignableFrom(tempClass);
             tempClass = tempClass.getSuperclass()) {
            fieldList.addAll(
                    Arrays.stream(tempClass.getDeclaredFields())
                            .filter(f -> Modifier.isPrivate(f.getModifiers())
                                    && !Modifier.isStatic(f.getModifiers()))
                            .collect(Collectors.toList())
            );
        }
        System.out.println(fieldList);
    }
}
