package cn.zhh.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import java.util.Date;

/**
 * 职位
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name = "shr_position")
public class Position extends BaseEntity {
    private String uniqueKey;

    private String name;

    private Byte source;

    private String salary;

    private String city;

    private String workExp;

    private String education;

    private String welfare;

    private String description;

    private String label;

    private String workAddress;

    private Date publishTime;

    private String url;

    private Long companyId;
}
