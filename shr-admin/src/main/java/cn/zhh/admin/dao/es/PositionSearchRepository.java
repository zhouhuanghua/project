package cn.zhh.admin.dao.es;

import cn.zhh.admin.dto.PositionSearchVO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * PositionSearchRepository
 *
 * @author Zhou Huanghua
 */
public interface PositionSearchRepository extends ElasticsearchRepository<PositionSearchVO, Long> {

}