package cn.zhh.admin.controller.portal;

import cn.zhh.admin.api.portal.SchoolInternshipApi;
import cn.zhh.admin.dto.req.SchoolInternshipCompanyReq;
import cn.zhh.admin.dto.req.SchoolInternshipJobListReq;
import cn.zhh.admin.dto.rsp.*;
import cn.zhh.admin.service.db.SchoolInternshipCompanyService;
import cn.zhh.admin.service.db.SchoolInternshipJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校招-实习-控制器
 *
 * @author Zhou Huanghua
 */
@RestController
@RequestMapping("/portal/school_internship")
public class SchoolInternshipController implements SchoolInternshipApi {

    @Autowired
    private SchoolInternshipCompanyService companyService;

    @Autowired
    private SchoolInternshipJobService jobService;

    @Override
    @PostMapping("/companies")
    public Response<Page<SchoolInternshipCompanyRsp>> pageQueryCompany(@RequestBody SchoolInternshipCompanyReq schoolInternshipCompanyReq) {
        return companyService.pageQuery(schoolInternshipCompanyReq);
    }

    @Override
    @PostMapping("/jobs")
    public Response<List<SchoolInternshipJobListRsp>> queryJobList(@RequestBody SchoolInternshipJobListReq schoolInternshipJobListReq) {
        return jobService.queryJobList(schoolInternshipJobListReq);
    }

    @Override
    @GetMapping("/jobs/{id:[\\d]+}")
    public Response<SchoolInternshipJobDetailRsp> queryJobDetail(@PathVariable Long id) {
        return jobService.queryJobDetail(id);
    }
}
