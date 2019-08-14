package cn.zhh.admin.api.portal;

import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.dto.req.PositionSearchReq;
import cn.zhh.admin.dto.rsp.Page;
import cn.zhh.admin.dto.rsp.PositionDetailRsp;
import cn.zhh.admin.dto.rsp.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Zhou Huanghua
 */
@Api(tags = "职位相关API")
public interface PositionApi {

    @ApiOperation("职位分页查询")
    Response<Page<PositionSearchVO>> pageQueryByCondition(PositionSearchReq positionSearchReq);

    @ApiOperation("根据id查询职位详情")
    Response<PositionDetailRsp> getDetailById(Long id);
}
