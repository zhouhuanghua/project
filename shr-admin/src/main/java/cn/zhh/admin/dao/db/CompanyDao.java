package cn.zhh.admin.dao.db;

import cn.zhh.admin.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CompanyDao
 *
 * @author Zhou Huanghua
 */
public interface CompanyDao extends JpaRepository<Company, Long> {
}