package cn.zhh.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 校招-实习-岗位
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name = "shr_school_internship_job")
public class SchoolInternshipJob extends BaseEntity {
    @Column
    private Long companyId;
    @Column
    private String name;
    @Column
    private String uniqueKey;
    @Column
    private String url;
    @Column
    private String workPlace;
    @Column
    private String deadline;
    @Column
    private String introduce;
    @Column
    private String description;
}
