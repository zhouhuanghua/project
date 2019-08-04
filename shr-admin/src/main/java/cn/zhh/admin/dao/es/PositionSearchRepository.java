package cn.zhh.admin.dao.es;

import cn.zhh.admin.dto.PositionSearchVO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PositionSearchRepository extends ElasticsearchRepository<PositionSearchVO, String> {

}