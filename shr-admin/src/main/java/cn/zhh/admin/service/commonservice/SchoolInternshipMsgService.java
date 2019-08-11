package cn.zhh.admin.service.commonservice;

import cn.zhh.common.dto.mq.SchoolInternshipMsg;

/**
 * 校招-实习消息服务接口
 *
 * @author Zhou Huanghua
 */
public interface SchoolInternshipMsgService {

    void process(SchoolInternshipMsg schoolInternshipMsg);
}
