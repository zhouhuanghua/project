package cn.zhh.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
    private String name;

    private String logo;

    private String developmentalStage;

    private String scale;

    private String domain;

    private String url;

    private String introduction;
}
