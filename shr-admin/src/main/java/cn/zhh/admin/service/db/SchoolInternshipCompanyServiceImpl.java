package cn.zhh.admin.service.db;

import cn.zhh.admin.dao.db.SchoolInternshipCompanyDao;
import cn.zhh.admin.dto.req.SchoolInternshipCompanyReq;
import cn.zhh.admin.dto.rsp.Page;
import cn.zhh.admin.dto.rsp.Response;
import cn.zhh.admin.dto.rsp.SchoolInternshipCompanyRsp;
import cn.zhh.admin.entity.SchoolInternshipCompany;
import cn.zhh.common.enums.IsDeletedEnum;
import cn.zhh.common.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 校招-实习-公司服务
 *
 * @author Zhou Huanghua
 */
@Service
@Slf4j
public class SchoolInternshipCompanyServiceImpl implements SchoolInternshipCompanyService {

    @Autowired
    private SchoolInternshipCompanyDao companyDao;

    @Override
    public JpaRepository<SchoolInternshipCompany, Long> dao() {
        return companyDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchoolInternshipCompany insert(SchoolInternshipCompany entity) {
        // 判断是否已经存在，如果存在则更新，否则插入
        Example<SchoolInternshipCompany> companyExample = buildExample(SchoolInternshipCompany.class,
                "uniqueKey", entity.getUniqueKey(), "isDeleted", IsDeletedEnum.NO.getCode());
        Optional<SchoolInternshipCompany> companyOptional = getByExample(companyExample);
        if (companyOptional.isPresent()) {
            log.info("校招-实习-公司{}已经存在！", entity.getUniqueKey());
            entity.setId(companyOptional.get().getId());
        }
        return save(entity);
    }

    @Override
    public Response<Page<SchoolInternshipCompanyRsp>> pageQuery(SchoolInternshipCompanyReq req) {
        SchoolInternshipCompany company = new SchoolInternshipCompany();
        // 设置招聘类型
        if (StringUtils.hasText(req.getHireType())) {
            company.setHireType(req.getHireType());
        }
        // 分页查询
        Example<SchoolInternshipCompany> example = Example.of(company);
        PageRequest pageRequest = PageRequest.of(req.getPageNum() - 1, req.getPageSize(),
                Sort.by(Sort.Order.desc("expiryDate")));
        org.springframework.data.domain.Page<SchoolInternshipCompany> originalPage = companyDao.findAll(example, pageRequest);
        // 结果转换
        Page<SchoolInternshipCompanyRsp> rspPage = Page.parse(originalPage).recordConvert(c -> {
            SchoolInternshipCompanyRsp rsp = new SchoolInternshipCompanyRsp();
            BeanUtils.copyProperties(c, rsp);
            return rsp;
        });
        // 返回
        return Response.ok(rspPage);
    }
}
