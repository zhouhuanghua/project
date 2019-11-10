package cn.zhh.admin.service.mq;

import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.entity.Company;
import cn.zhh.admin.entity.Position;
import cn.zhh.admin.service.db.CompanyService;
import cn.zhh.admin.service.db.PositionService;
import cn.zhh.admin.service.es.PositionSearchService;
import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.enums.IsDeletedEnum;
import cn.zhh.common.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 职位信息消息服务
 *
 * @author Zhou Huanghua
 */
@Service
@Slf4j
public class PositionInfoMsgServiceImpl implements PositionInfoMsgService {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private PositionSearchService positionSearchService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(PositionInfoMsg positionInfoMsg) {
        // 1、插入公司记录
        Company company = new Company();
        BeanUtils.copySelectedProperties(positionInfoMsg, company,
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
        BeanUtils.copyProperties(positionInfoMsg, position);
        position.setCreator(SysConsts.DEFAULT_USER_NAME);
        position.setCreateTime(new Date());
        position.setIsDeleted(IsDeletedEnum.NO.getCode());
        position.setCompanyId(company.getId());
        positionService.insert(position);

        // 3、数据插入ES
        PositionSearchVO positionSearchVO = new PositionSearchVO();
        BeanUtils.copyProperties(positionInfoMsg, positionSearchVO);
        positionSearchVO.setId(position.getId());
        positionSearchService.insert(positionSearchVO);
    }
}
