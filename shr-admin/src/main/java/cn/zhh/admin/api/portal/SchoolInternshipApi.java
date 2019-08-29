package cn.zhh.admin.api.portal;

import cn.zhh.admin.dto.req.SchoolInternshipCompanyReq;
import cn.zhh.admin.dto.req.SchoolInternshipJobListReq;
import cn.zhh.admin.dto.rsp.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

/**
 * @author Zhou Huanghua
 */
@Api(tags = "实习-校招-API")
public interface SchoolInternshipApi {

    @ApiOperation("分页查询实习校招公司")
    Response<Page<SchoolInternshipCompanyRsp>> pageQueryCompany(SchoolInternshipCompanyReq schoolInternshipCompanyReq);

    @ApiOperation("查询岗位列表")
    Response<List<SchoolInternshipJobListRsp>> queryJobList(SchoolInternshipJobListReq schoolInternshipJobListReq);

    @ApiOperation("查询岗位详情")
    Response<SchoolInternshipJobDetailRsp> queryJobDetail(Long id);
}
