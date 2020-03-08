package cn.zhh.portal.service;

import cn.zhh.portal.dto.PositionSearchReq;
import cn.zhh.portal.dto.PositionSearchVO;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * 职位搜索服务接口类
 *
 * @author Zhou Huanghua
 */
public interface IPositionSearchService {

    PositionSearchVO insert(PositionSearchVO positionSearchVO);

    Page<PositionSearchVO> pageQueryByCondition(PositionSearchReq positionSearchReq);

    Optional<PositionSearchVO> getById(Long id);
}
