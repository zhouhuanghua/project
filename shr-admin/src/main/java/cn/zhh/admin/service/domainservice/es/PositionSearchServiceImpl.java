package cn.zhh.admin.service.domainservice.es;

import cn.zhh.admin.dao.es.PositionSearchRepository;
import cn.zhh.admin.dto.PositionSearchVO;
import cn.zhh.admin.dto.req.PositionSearchReq;
import cn.zhh.admin.dto.rsp.Page;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        if (StringUtils.hasText(positionSearchReq.getCity())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("city", positionSearchReq.getCity()));
        }
        // 工作经验
        if (StringUtils.hasText(positionSearchReq.getWorkExp())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("workExp", positionSearchReq.getWorkExp()));
        }
        // 学历
        if (StringUtils.hasText(positionSearchReq.getEducation())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("education", positionSearchReq.getEducation()));
        }
        // 公司规模
        if (StringUtils.hasText(positionSearchReq.getCompanyScale())) {
            boolQueryBuilder.must(QueryBuilders.termQuery("companyScale", positionSearchReq.getCompanyScale()));
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
