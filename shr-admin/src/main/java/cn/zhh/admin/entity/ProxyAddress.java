package cn.zhh.admin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
    private String ip;
    private String port;
    private String type;
}