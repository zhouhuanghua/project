package cn.zhh.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 代理地址实体类
 *
 * @author Zhou Huanghua
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name = "shr_proxy_address")
public class ProxyAddress extends BaseEntity {
    @Column
    private String ip;
    @Column
    private String port;
    @Column
    private String type;
}