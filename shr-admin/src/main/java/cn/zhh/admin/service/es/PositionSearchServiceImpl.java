package cn.zhh.admin.service.es;

import cn.zhh.admin.dao.es.PositionSearchRepository;
import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.dto.req.PositionSearchReq;
import cn.zhh.admin.dto.rsp.Page;
import cn.zhh.common.enums.DevelopmentStageEnum;
import cn.zhh.common.enums.EducationEnum;
import cn.zhh.common.enums.WorkExpEnum;
import cn.zhh.common.util.OptionalOperationUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 职位搜索服务
 *
 * @author Zhou Huanghua
 */
@Service
public class PositionSearchServiceImpl implements PositionSearchService {

    @Autowired
    private PositionSearchRepository positionSearchRepository;

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
            boolQueryBuilder.must(QueryBuilders.termQuery("city", city));
        });
        // 工作经验
        final Byte workExpCode = positionSearchReq.getWorkExp();
        if (Objects.nonNull(workExpCode) && !Objects.equals(workExpCode, WorkExpEnum.ALL.getCode())) {
            OptionalOperationUtils.consumeIfNonNull(WorkExpEnum.code2desc(workExpCode), workExpDesc -> {
                boolQueryBuilder.must(QueryBuilders.termQuery("workExp", workExpDesc));
            });
        }
        // 学历
        final Byte educationCode = positionSearchReq.getEducation();
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
        }
        // 分页
        PageRequest pageRequest = PageRequest.of(positionSearchReq.getPageNum()-1, positionSearchReq.getPageSize(),
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
}
