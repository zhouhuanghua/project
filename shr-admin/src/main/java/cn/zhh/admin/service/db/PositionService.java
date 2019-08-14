package cn.zhh.admin.service.db;

import cn.zhh.admin.dto.rsp.PositionDetailRsp;
import cn.zhh.admin.entity.Position;
import cn.zhh.admin.service.BaseService;

/**
 * 职位服务接口
 *
 * @author Zhou Huanghua
 */
public interface PositionService extends BaseService<Position, Long> {

    /**
     * 根据id查询职位详情
     *
     * @param id 职位id
     * @return Map
     *              ——position：职位信息
     *              ——company：公司信息
     */
    PositionDetailRsp getDetailById(Long id);
}
