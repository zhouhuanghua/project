package cn.zhh.admin.service.domainservice;

import cn.zhh.admin.dao.db.PositionDao;
import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.entity.Company;
import cn.zhh.admin.entity.Position;
import cn.zhh.admin.service.domainservice.es.PositionSearchService;
import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.enums.IsDeletedEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

;

/**
 * 职位服务
 *
 * @author Zhou Huanghua
 */
@Service
@Slf4j
public class PositionServiceImpl implements PositionService {

    @Autowired
    private PositionDao dao;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PositionSearchService positionSearchService;

    @Override
    public JpaRepository<Position, Long> dao() {
        return dao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(PositionInfoMsg positionInfo) {
        // 1、插入公司记录
        Company company = new Company();
        BeanUtils.copySelectedProperties(positionInfo, company,
                "companyName:name", "companyLogo:logo",
                "companyDevelopmentalStage:developmentalStage", "companyScale:scale",
                "companyDomain:domain", "companyUrl:url",
                "companyIntroduction:introduction");
        company.setCreator(SysConsts.DEFAULT_USER_NAME);
        company.setCreateTime(new Date());
        company.setIsDeleted(IsDeletedEnum.NO.getCode());
        companyService.insert(company);

        // 2、插入职位记录
        Position position = new Position();
        BeanUtils.copyProperties(positionInfo, position);
        position.setCreator(SysConsts.DEFAULT_USER_NAME);
        position.setCreateTime(new Date());
        position.setIsDeleted(IsDeletedEnum.NO.getCode());
        position.setCompanyId(company.getId());
        insert(position);

        // 3、数据插入ES
        PositionSearchVO positionSearchVO = new PositionSearchVO();
        BeanUtils.copyProperties(position, positionSearchVO);
        positionSearchVO.setId(String.valueOf(position.getId()));
        positionSearchService.save(positionSearchVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Position insert(Position entity) {
        // 判断是否已经存在，如果存在直接返回原数据
        Example<Position> positionExample = buildExample(Position.class, "source", entity.getSource(),
                "uniqueKey", entity.getUniqueKey(), "isDeleted", IsDeletedEnum.NO.getCode());
        Optional<Position> positionOptional = getByExample(positionExample);
        if (positionOptional.isPresent()) {
            log.info("职位已经存在，来源：{}，名称{}", PositionSourceEnum.code2desc(entity.getSource()), entity.getName());
            return positionOptional.get();
        }
        // 插入数据库
        return save(entity);
    }
}
