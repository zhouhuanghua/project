package cn.zhh.admin.controller.portal;

import cn.zhh.admin.api.portal.PositionApi;
import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.dto.req.PositionSearchReq;
import cn.zhh.admin.dto.rsp.Page;
import cn.zhh.admin.dto.rsp.PositionDetailRsp;
import cn.zhh.admin.dto.rsp.Response;
import cn.zhh.admin.service.db.PositionService;
import cn.zhh.admin.service.es.PositionSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private PositionService positionService;

    @Override
    @PostMapping("/query")
    public Response<Page<PositionSearchVO>> pageQueryByCondition(@RequestBody PositionSearchReq positionSearchReq) {
        return Response.ok(positionSearchService.pageQueryByCondition(positionSearchReq));
    }

    @Override
    @GetMapping("/{id:[\\d+]}")
    public Response<PositionDetailRsp> getDetailById(@PathVariable Long id) {
        return Response.ok(positionService.getDetailById(id));
    }
}
