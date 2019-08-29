package cn.zhh.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 校招-实习-公司
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name = "shr_school_internship_company")
public class SchoolInternshipCompany extends BaseEntity {
    @Column
    private String name;
    @Column
    private String uniqueKey;
    @Column
    private String url;
    @Column
    private String logoUrl;
    @Column
    private String workPlace;
    @Column
    private String industry;
    @Column
    private Integer jobNum;
    @Column
    private String expiryDate;
    @Column
    private String introduce;
    @Column
    private String label;
    @Column
    private String hireType;
    @Column
    private String jobTypes;
}
