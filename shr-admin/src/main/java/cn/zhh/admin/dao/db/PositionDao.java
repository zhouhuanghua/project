package cn.zhh.admin.dao.db;

import cn.zhh.admin.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PositionDao
 *
 * @author Zhou Huanghua
 */
public interface PositionDao extends JpaRepository<Position, Long> {
}