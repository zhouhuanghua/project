package cn.zhh.admin.service.db;

import cn.zhh.admin.dao.db.CompanyDao;
import cn.zhh.admin.entity.Company;
import cn.zhh.common.enums.IsDeletedEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

;

/**
 * 公司服务
 *
 * @author Zhou Huanghua
 */
@Service
@Slf4j
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyDao companyDao;

    @Override
    public JpaRepository<Company, Long> dao() {
        return companyDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Company insert(Company entity) {
        // 判断是否已经存在，如果存在则更新，否则插入
        Example<Company> companyExample = buildExample(Company.class,
                "name", entity.getName(), "isDeleted", IsDeletedEnum.NO.getCode());
        Optional<Company> companyOptional = getByExample(companyExample);
        if (companyOptional.isPresent()) {
            log.info("公司【{}】已经存在！", entity.getName());
            entity.setId(companyOptional.get().getId());
        }
        // 长度截取
        String introduction = entity.getIntroduction();
        if (StringUtils.hasText(introduction) && introduction.length() > 8000) {
            entity.setIntroduction(introduction.substring(0, 7800) + "......更多详情请查看公司官网。");
        }

        return save(entity);
    }
}
