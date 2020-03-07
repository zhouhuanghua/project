package cn.zhh.portal.dao;

import cn.zhh.portal.dto.PositionSearchVO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 职位搜索DAO
 *
 * @author Zhou Huanghua
 */
public interface PositionSearchRepository extends ElasticsearchRepository<PositionSearchVO, Long> {
}
