package cn.zhh.admin.api.portal;

import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.dto.req.PositionSearchReq;
import cn.zhh.admin.dto.rsp.Page;
import cn.zhh.admin.dto.rsp.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
@Api(tags = "职位相关API")
public interface PositionApi {

    @ApiOperation("分页查询")
    @PostMapping("/query")
    Response<Page<PositionSearchVO>> pageQueryByCondition(@RequestBody PositionSearchReq positionSearchReq);
}
