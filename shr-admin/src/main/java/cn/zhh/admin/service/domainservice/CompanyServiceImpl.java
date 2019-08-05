package cn.zhh.admin.service.domainservice;

import cn.zhh.admin.dao.db.CompanyDao;
import cn.zhh.admin.entity.Company;
import cn.zhh.common.enums.IsDeletedEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private CompanyDao dao;

    @Override
    public JpaRepository<Company, Long> dao() {
        return dao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Company insert(Company entity) {
        // 判断是否已经存在，如果存在直接返回原数据
        Example<Company> companyExample = buildExample(Company.class,
                "name", entity.getName(), "isDeleted", IsDeletedEnum.NO.getCode());
        Optional<Company> companyOptional = getByExample(companyExample);
        if (companyOptional.isPresent()) {
            log.info("公司({})已经存在！", entity.getName());
            return companyOptional.get();
        }
        // 插入数据库
        return save(entity);
    }
}
