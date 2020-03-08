package cn.zhh.portal.service;

import cn.zhh.common.constant.CityEnum;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.portal.constant.KeyWordTypeEnum;
import cn.zhh.portal.dao.PositionSearchRepository;
import cn.zhh.portal.dto.PositionSearchReq;
import cn.zhh.portal.dto.PositionSearchVO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 职位搜索服务实现类
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Service
public class PositionSearchServiceImpl implements IPositionSearchService {

    @Autowired
    private PositionSearchRepository repository;

    @Override
    public PositionSearchVO insert(PositionSearchVO positionSearchVO) {
        return repository.save(positionSearchVO);
    }

    @Override
    public Page<PositionSearchVO> pageQueryByCondition(PositionSearchReq positionSearchReq) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String content = positionSearchReq.getContent();
        if (StringUtils.hasText(content)) {
            Optional<KeyWordTypeEnum> enumOptional = KeyWordTypeEnum.getByCode(positionSearchReq.getKwType());
            if (enumOptional.isPresent()) {
                switch (enumOptional.get()) {
                    case POSITION : boolQueryBuilder.must(QueryBuilders.matchQuery("name", content)); break;
                    case COMPANY: boolQueryBuilder.must(QueryBuilders.matchQuery("companyName", content)); break;
                    case DESCRIPTION: boolQueryBuilder.must(QueryBuilders.matchQuery("description", content)); break;
                }
            } else {
                boolQueryBuilder.must(QueryBuilders.multiMatchQuery(content, "name", "companyName"));
            }
        }
        // 城市
        OptionalOperationUtils.consumeIfNonNull(positionSearchReq.getCityCode(), cityCode -> {
            Optional<String> optional = CityEnum.code2Desc(cityCode);
            if (optional.isPresent()) {
                boolQueryBuilder.must(QueryBuilders.termQuery("city.keyword", optional.get()));
            }
        });
        // 分页
        PageRequest pageRequest = PageRequest.of(positionSearchReq.getPageNo(), positionSearchReq.getPageSize(),
                Sort.by(Sort.Order.desc("publishTime")));

        // 查询
        return repository.search(boolQueryBuilder, pageRequest);
    }

    @Override
    public Optional<PositionSearchVO> getById(Long id) {
        return repository.findById(id);
    }
}
