package cn.zhh.admin.dao.db;

import cn.zhh.admin.entity.SchoolInternshipCompany;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SchoolInternshipCompanyDao
 *
 * @author Zhou Huanghua
 */
public interface SchoolInternshipCompanyDao extends JpaRepository<SchoolInternshipCompany, Long> {
}