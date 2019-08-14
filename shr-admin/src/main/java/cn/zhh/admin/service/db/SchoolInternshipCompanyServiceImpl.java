package cn.zhh.admin.service.db;

import cn.zhh.admin.dao.db.SchoolInternshipCompanyDao;
import cn.zhh.admin.entity.SchoolInternshipCompany;
import cn.zhh.common.enums.IsDeletedEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private SchoolInternshipCompanyDao schoolInternshipCompanyDao;

    @Override
    public JpaRepository<SchoolInternshipCompany, Long> dao() {
        return schoolInternshipCompanyDao;
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
}
