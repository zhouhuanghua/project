package cn.zhh.admin.service.es;

import cn.zhh.admin.dao.es.PositionSearchRepository;
import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.dto.req.PositionSearchReq;
import cn.zhh.admin.dto.rsp.Page;
import cn.zhh.admin.entity.Company;
import cn.zhh.admin.entity.Position;
import cn.zhh.admin.service.db.CompanyService;
import cn.zhh.admin.service.db.PositionService;
import cn.zhh.common.enums.IsDeletedEnum;
import cn.zhh.common.enums.WorkExpEnum;
import cn.zhh.common.util.BeanUtils;
import cn.zhh.common.util.OptionalOperationUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 职位搜索服务
 *
 * @author Zhou Huanghua
 */
@Service
@Slf4j
public class PositionSearchServiceImpl implements PositionSearchService {

    @Autowired
    private PositionSearchRepository positionSearchRepository;

    @Autowired
    private PositionService positionService;

    @Autowired
    private CompanyService companyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PositionSearchVO insert(PositionSearchVO positionSearchVO) {
        return positionSearchRepository.save(positionSearchVO);
    }

    @Override
    public Page<PositionSearchVO> pageQueryByCondition(PositionSearchReq positionSearchReq) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 内容搜索职位名称或者公司名称
        String content = positionSearchReq.getContent();
        if (StringUtils.hasText(content)) {
            boolQueryBuilder.must(QueryBuilders.multiMatchQuery(content, "name", "companyName"));
        }
        // 城市
        OptionalOperationUtils.consumeIfNotBlank(positionSearchReq.getCity(), city -> {
            boolQueryBuilder.must(QueryBuilders.matchQuery("city", city));
        });
        // 工作经验
        OptionalOperationUtils.consumeIfNotEmpty(positionSearchReq.getWorkExpList(), workExpCodeList -> {
            BoolQueryBuilder workExpQuery = QueryBuilders.boolQuery();
            for (Byte workExpCode : workExpCodeList) {
                workExpQuery.should(QueryBuilders.termQuery("workExp", WorkExpEnum.code2desc(workExpCode)));
            }
            boolQueryBuilder.must(workExpQuery);
        });
        // 学历
        /*final Byte educationCode = positionSearchReq.getEducation();
        if (Objects.nonNull(educationCode) && !Objects.equals(educationCode, EducationEnum.ALL.getCode())) {
            OptionalOperationUtils.consumeIfNonNull(EducationEnum.code2desc(educationCode), educationDesc -> {
                boolQueryBuilder.must(QueryBuilders.termQuery("education", educationDesc));
            });
        }
        // 公司发展阶段
        final Byte developmentStageCode = positionSearchReq.getCompanyDevelopmentStage();
        if (Objects.nonNull(developmentStageCode) && !Objects.equals(developmentStageCode, DevelopmentStageEnum.ALL.getCode())) {
            OptionalOperationUtils.consumeIfNonNull(EducationEnum.code2desc(developmentStageCode), developmentStageDesc -> {
                boolQueryBuilder.must(QueryBuilders.termQuery("companyDevelopmentStage", developmentStageDesc));
            });
        }*/
        // 分页
        PageRequest pageRequest = PageRequest.of(positionSearchReq.getPageNum() - 1, positionSearchReq.getPageSize(),
                Sort.by(Sort.Order.desc("publishTime")));

        // 查询并转化
        org.springframework.data.domain.Page<PositionSearchVO> originalPage = positionSearchRepository.search(boolQueryBuilder, pageRequest);
        return Page.parse(originalPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear() {
        positionSearchRepository.deleteAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        positionSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshDb2Es() {
        log.info("将DB数据刷到ES开始...");
        positionSearchRepository.deleteAll();

        List<Position> positionList = positionService.listByExample(positionService.buildExample(Position.class, "isDeleted", IsDeletedEnum.NO.getCode()));
        for (Position position : positionList) {
            Company company = companyService.getById(position.getCompanyId());
            PositionSearchVO vo = new PositionSearchVO();
            BeanUtils.copyProperties(position, vo);
            vo.setCompanyName(company.getName());
            vo.setCompanyLogo(company.getLogo());
            vo.setCompanyDevelopmentStage(company.getDevelopmentalStage());
            positionSearchRepository.save(vo);
        }

        log.info("将DB数据刷到ES结束！");
    }


}
