package cn.zhh.admin.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Date;

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
    private String creator;
    private Date createTime;
    private String lastUpdateUser;
    private Date lastUpdateTime;
    private Byte isDeleted;
}
