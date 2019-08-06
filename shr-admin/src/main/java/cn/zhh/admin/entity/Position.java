package cn.zhh.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
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
    @Column
    private String uniqueKey;
    @Column
    private String name;
    @Column
    private Byte source;
    @Column
    private String salary;
    @Column
    private String city;
    @Column
    private String workExp;
    @Column
    private String education;
    @Column
    private String welfare;
    @Column
    private String description;
    @Column
    private String label;
    @Column
    private String workAddress;
    @Column
    private Date publishTime;
    @Column
    private String url;
    @Column
    private Long companyId;
}
