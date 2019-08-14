package cn.zhh.admin.service.es;

import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.dto.req.PositionSearchReq;
import cn.zhh.admin.dto.rsp.Page;

/**
 * 职位搜索服务接口
 *
 * @author Zhou Huanghua
 */
public interface PositionSearchService {

    PositionSearchVO insert(PositionSearchVO positionSearchVO);

    Page<PositionSearchVO> pageQueryByCondition(PositionSearchReq positionSearchReq);

    void clear();

    void deleteById(Long id);
}
