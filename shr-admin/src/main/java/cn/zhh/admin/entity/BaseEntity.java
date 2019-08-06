package cn.zhh.admin.entity;

import cn.zhh.common.enums.ErrorEnum;
import cn.zhh.common.util.BusinessException;
import lombok.Data;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * 基础实体类，属性已经设置默认值
 *
 * @author Zhou Huanghua
 */
@Data
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String creator;
    @Column
    private Date createTime;
    @Column
    private String lastUpdateUser;
    @Column
    private Date lastUpdateTime;
    @Column
    private Byte isDeleted;

    public void setDefaultValue() {
        Arrays.stream(this.getClass().getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(Column.class))
            .forEach(field -> {
                field.setAccessible(true);
                try {
                    if (Objects.nonNull(field.get(this))) {
                        return;
                    }
                    Class<?> fieldType = field.getType();
                    if (Objects.equals(fieldType, Date.class)) {
                        field.set(this, new Date());
                    }
                    if (Objects.equals(fieldType, String.class)) {
                        field.set(this, "");
                    }
                } catch (IllegalAccessException e) {
                    throw new BusinessException(ErrorEnum.BAD_REQUEST, e);
                }
            });
    }
}
