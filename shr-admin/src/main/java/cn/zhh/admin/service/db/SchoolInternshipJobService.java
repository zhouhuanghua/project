package cn.zhh.admin.service.db;

import cn.zhh.admin.dto.req.SchoolInternshipJobListReq;
import cn.zhh.admin.dto.rsp.Response;
import cn.zhh.admin.dto.rsp.SchoolInternshipJobDetailRsp;
import cn.zhh.admin.dto.rsp.SchoolInternshipJobListRsp;
import cn.zhh.admin.entity.SchoolInternshipJob;
import cn.zhh.admin.service.BaseService;

import java.util.List;

/**
 * 校招-实习-岗位服务接口
 *
 * @author Zhou Huanghua
 */
public interface SchoolInternshipJobService extends BaseService<SchoolInternshipJob, Long> {

    /**
     * 查询岗位列表
     *
     * @param req 请求参数对象
     * @return
     */
    Response<List<SchoolInternshipJobListRsp>> queryJobList(SchoolInternshipJobListReq req);

    /**
     * 查询岗位详情
     *
     * @param id 岗位id
     * @return
     */
    Response<SchoolInternshipJobDetailRsp> queryJobDetail(Long id);
}
