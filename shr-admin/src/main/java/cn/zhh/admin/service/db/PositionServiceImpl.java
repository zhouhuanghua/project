package cn.zhh.admin.service.db;

import cn.zhh.admin.dao.db.PositionDao;
import cn.zhh.admin.dto.rsp.PositionDetailRsp;
import cn.zhh.admin.entity.Company;
import cn.zhh.admin.entity.Position;
import cn.zhh.common.enums.IsDeletedEnum;
import cn.zhh.common.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private PositionDao positionDao;

    @Autowired
    private CompanyService companyService;

    @Override
    public JpaRepository<Position, Long> dao() {
        return positionDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Position insert(Position entity) {
        synchronized (entity.getSource() + entity.getUniqueKey()) {
            // 判断是否已经存在，如果存在则更新，否则插入
            Example<Position> positionExample = buildExample(Position.class, "source", entity.getSource(),
                    "uniqueKey", entity.getUniqueKey(), "isDeleted", IsDeletedEnum.NO.getCode());
            Optional<Position> positionOptional = getByExample(positionExample);
            if (positionOptional.isPresent()) {
                log.info("岗位【{}】已经存在！", entity.getUniqueKey());
                entity.setId(positionOptional.get().getId());
            }
            return save(entity);
        }
    }

    @Override
    public PositionDetailRsp getDetailById(Long id) {
        PositionDetailRsp rsp = new PositionDetailRsp();
        // 职位
        Position position = getById(id);
        PositionDetailRsp.PositionVO positionVO = new PositionDetailRsp.PositionVO();
        BeanUtils.copyProperties(position, positionVO);
        // 公司
        Company company = companyService.getById(position.getCompanyId());
        PositionDetailRsp.CompanyVO companyVo = new PositionDetailRsp.CompanyVO();
        BeanUtils.copyProperties(company, companyVo);
        // 公司评论 todo
        companyVo.setCommentList(null);

        rsp.setPosition(positionVO);
        rsp.setCompany(companyVo);
        return rsp;
    }
}
