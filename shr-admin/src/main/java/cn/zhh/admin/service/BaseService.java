package cn.zhh.admin.service;

import cn.zhh.admin.entity.BaseEntity;
import cn.zhh.common.enums.ErrorEnum;
import cn.zhh.common.enums.IsDeletedEnum;
import cn.zhh.common.util.BusinessException;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 基础服务
 *
 * @author Zhou Huanghua
 */
public interface BaseService<T extends BaseEntity, ID> {

    default void breaks(ErrorEnum errorEnum) {
        throw new BusinessException(errorEnum);
    }

    default void breaks(ErrorEnum errorEnum, String msg) {
        throw new BusinessException(errorEnum, msg);
    }

    abstract JpaRepository<T, ID> dao();

    default T getById(ID id) {
        Optional<T> optional = dao().findById(id);
        if (!optional.isPresent()) {
            breaks(ErrorEnum.DB_RECORD_NOT_EXIST, "数据不存在！");
        }
        T t = optional.get();
        if (Objects.equals(t.getIsDeleted(), IsDeletedEnum.YES.getCode())) {
            breaks(ErrorEnum.DB_RECORD_NOT_RIGHT, "数据不存在！");
        }
        return t;
    }

    /**
     * 插入一条记录
     *
     * @param entity
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    default T insert(T entity) {
        if (Objects.nonNull(entity.getId())) {
            breaks(ErrorEnum.INVALID_PARAM, "待插入的实体对象已存在id！");
        }
        return save(entity);
    }

    /**
     * 根据id更新记录
     *
     * @param entity
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    default T updateById(T entity) {
        if (Objects.isNull(entity.getId())) {
            breaks(ErrorEnum.INVALID_PARAM, "待更新的实体对象缺失id！");
        }
        return save(entity);
    }

    default T save(T entity) {
        return dao().save(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    default void deleteById(ID id) {
        dao().deleteById(id);
    }

    default boolean exists(Example<T> example) {
        example.getProbe().setIsDeleted(IsDeletedEnum.NO.getCode());
        return dao().exists(example);
    }

    default Optional<T> getByExample(Example<T> example) {
        example.getProbe().setIsDeleted(IsDeletedEnum.NO.getCode());
        return dao().findOne(example);
    }

    default List<T> listByExample(Example<T> example) {
        example.getProbe().setIsDeleted(IsDeletedEnum.NO.getCode());
        return dao().findAll(example);
    }

    /**
     * 建立Example对象
     *
     * @param entityClass Example对应实体的Class对象
     * @param args 属性键值对：如("name", "张三", "age", "28")，一个name一个value依次填写
     * @return
     */
    default Example<T> buildExample(Class<T> entityClass, Object... args) {
        // 参数不为空并且数量为偶数
        if (Objects.isNull(args) || args.length % 2 != 0) {
            breaks(ErrorEnum.INVALID_PARAM, "属性键值对不能为空且数量必须为偶数！");
        }
        // 反射创建对象
        T entity = org.springframework.beans.BeanUtils.instantiateClass(entityClass);
        // 设置属性
        for (int i = 0, end = args.length - 1; i < end; i += 2) {
            String fieldName = (String) args[i];
            Object fieldValue = args[i+1];
            PropertyDescriptor propertyDescriptor = org.springframework.beans.BeanUtils.getPropertyDescriptor(entityClass, fieldName);
            Method writeMethod = propertyDescriptor.getWriteMethod();
            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                writeMethod.setAccessible(true);
            }
            ReflectionUtils.invokeMethod(writeMethod, entity, fieldValue);
        }

        return Example.of(entity);
    }
}
