package cn.zhh.admin.controller.portal;

import cn.zhh.admin.api.portal.PositionApi;
import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.dto.req.PositionSearchReq;
import cn.zhh.admin.dto.rsp.Page;
import cn.zhh.admin.dto.rsp.Response;
import cn.zhh.admin.service.domainservice.es.PositionSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 职位控制器
 *
 * @author Zhou Huanghua
 */
@RestController
@RequestMapping("/portal/positions")
public class PositionController implements PositionApi {

    @Autowired
    private PositionSearchService positionSearchService;

    @Override
    public Response<Page<PositionSearchVO>> pageQueryByCondition(@RequestBody  PositionSearchReq positionSearchReq) {
        return Response.ok(positionSearchService.pageQueryByCondition(positionSearchReq));
    }
}
