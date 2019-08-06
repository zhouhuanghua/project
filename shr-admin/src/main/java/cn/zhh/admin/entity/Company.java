package cn.zhh.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 公司
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name = "shr_company")
public class Company extends BaseEntity {
    @Column
    private String name;
    @Column
    private String logo;
    @Column
    private String developmentalStage;
    @Column
    private String scale;
    @Column
    private String domain;
    @Column
    private String url;
    @Column
    private String introduction;
}
