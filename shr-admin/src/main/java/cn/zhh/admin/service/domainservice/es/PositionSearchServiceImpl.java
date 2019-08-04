package cn.zhh.admin.service.domainservice.es;

import cn.zhh.admin.dao.es.PositionSearchRepository;
import cn.zhh.admin.dto.PositionSearchVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 职位搜索服务
 *
 * @author Zhou Huanghua
 */
@Service
public class PositionSearchServiceImpl implements PositionSearchService {

    @Autowired
    private PositionSearchRepository positionSearchRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PositionSearchVO save(PositionSearchVO positionSearchVO) {
        return positionSearchRepository.save(positionSearchVO);
    }
}
