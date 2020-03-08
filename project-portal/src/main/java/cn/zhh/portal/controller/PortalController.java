package cn.zhh.portal.controller;

import cn.zhh.common.util.ThrowableUtils;
import cn.zhh.portal.dto.*;
import cn.zhh.portal.service.IPositionSearchService;
import cn.zhh.portal.util.ObjCopyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

/**
 * 控制器
 *
 * @author Zhou Huanghua
 */
@Slf4j
@RestController
public class PortalController {

    @Autowired
    private IPositionSearchService positionSearchService;

    /**
     * 首页跳转
     *
     * @return 跳转逻辑视图
     */
    @RequestMapping("/")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("redirect:index.html");
        return modelAndView;
    }

    /**
     * 分页查询
     *
     * @param req
     * @return Result<Page<PositionSearchSimpleRsp>>
     */
    @PostMapping("/position")
    public Result<Page<PositionSearchSimpleRsp>> pageQuery(@RequestBody PositionSearchReq req) {
        Page<PositionSearchSimpleRsp> rspPage = positionSearchService.pageQueryByCondition(req)
                .map(vo -> {
                    try {
                        return ObjCopyUtils.copyProperties(vo, PositionSearchSimpleRsp.class, false);
                    } catch (IllegalAccessException | InstantiationException e) {
                        log.error("反射创建对象异常，e={}", ThrowableUtils.getStackTrace(e));
                    }
                    return ObjCopyUtils.copyProperties(vo, new PositionSearchSimpleRsp(), false);
                });
        return Result.ok(rspPage);
    }

    /**
     * 根据ID查询
     *
     * @param id ID
     * @return Result<Page<PositionSearchSimpleRsp>>
     */
    @GetMapping("/position/{id:[\\d]+}")
    public Result<PositionSearchDetailRsp> queryById(@PathVariable("id") Long id) {
        Optional<PositionSearchVO> voOptional = positionSearchService.getById(id);
        if (voOptional.isPresent()) {
            PositionSearchDetailRsp rsp;
            try {
                rsp = ObjCopyUtils.copyProperties(voOptional.get(), PositionSearchDetailRsp.class, true);
            } catch (IllegalAccessException | InstantiationException e) {
                log.error("反射创建对象异常，e={}", ThrowableUtils.getStackTrace(e));
                rsp = ObjCopyUtils.copyProperties(voOptional.get(), new PositionSearchDetailRsp(), true);;
            }
            return Result.ok(rsp);
        }
        return Result.err("数据不存在！");
    }
}
