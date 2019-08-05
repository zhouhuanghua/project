package cn.zhh.admin.service.commonservice;

import cn.zhh.common.dto.mq.PositionInfoMsg;

/**
 * 职位信息服务接口
 *
 * @author Zhou Huanghua
 */
public interface PositionInfoService {

    void process(PositionInfoMsg positionInfoMsg);
}
