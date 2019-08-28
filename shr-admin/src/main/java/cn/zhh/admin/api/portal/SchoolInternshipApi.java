package cn.zhh.admin.api.portal;

import cn.zhh.admin.dto.req.SchoolInternshipCompanyReq;
import cn.zhh.admin.dto.rsp.Page;
import cn.zhh.admin.dto.rsp.Response;
import cn.zhh.admin.dto.rsp.SchoolInternshipCompanyRsp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Zhou Huanghua
 */
@Api(tags = "实习-校招-API")
public interface SchoolInternshipApi {

    @ApiOperation("分页查询实习校招公司分页查询")
    Response<Page<SchoolInternshipCompanyRsp>> pageQueryCompany(SchoolInternshipCompanyReq schoolInternshipCompanyReq);

    @ApiOperation("查询岗位列表")
    Response<List<SchoolInternshipJobListRsp>> queryPositionList(Long id);

    @ApiOperation("查询岗位详情")
    Response<SchoolInternshipJobDetailRsp> queryPositionDetail(Long id);
}
