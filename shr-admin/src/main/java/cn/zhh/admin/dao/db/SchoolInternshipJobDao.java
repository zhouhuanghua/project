package cn.zhh.admin.dao.db;

import cn.zhh.admin.entity.SchoolInternshipJob;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SchoolInternshipJobDao
 *
 * @author Zhou Huanghua
 */
public interface SchoolInternshipJobDao extends JpaRepository<SchoolInternshipJob, Long> {
}