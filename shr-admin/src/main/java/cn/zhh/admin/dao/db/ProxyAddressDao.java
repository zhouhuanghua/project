package cn.zhh.admin.dao.db;

import cn.zhh.admin.entity.ProxyAddress;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ProxyAddressDao
 *
 * @author Zhou Huanghua
 */
public interface ProxyAddressDao extends JpaRepository<ProxyAddress, Long> {
}