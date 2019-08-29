package cn.zhh.admin.service.db;

import cn.zhh.admin.dto.req.SchoolInternshipCompanyReq;
import cn.zhh.admin.dto.rsp.Page;
import cn.zhh.admin.dto.rsp.Response;
import cn.zhh.admin.dto.rsp.SchoolInternshipCompanyRsp;
import cn.zhh.admin.entity.SchoolInternshipCompany;
import cn.zhh.admin.service.BaseService;

/**
 * 校招-实习-公司服务接口
 *
 * @author Zhou Huanghua
 */
public interface SchoolInternshipCompanyService extends BaseService<SchoolInternshipCompany, Long> {

    /**
     * 分页查询
     *
     * @param req 请求参数对象
     * @return
     */
    Response<Page<SchoolInternshipCompanyRsp>> pageQuery(SchoolInternshipCompanyReq req);
}
