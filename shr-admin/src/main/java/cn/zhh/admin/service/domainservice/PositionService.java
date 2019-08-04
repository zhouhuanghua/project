package cn.zhh.admin.service.domainservice;

import cn.zhh.admin.entity.Position;
import cn.zhh.admin.service.BaseService;
import cn.zhh.common.dto.mq.PositionInfoMsg;

/**
 * 职位服务接口
 *
 * @author Zhou Huanghua
 */
public interface PositionService extends BaseService<Position, Long> {

    void add(PositionInfoMsg positionInfo);
}
