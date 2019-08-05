package cn.zhh.admin.service.domainservice;

import cn.zhh.admin.dao.db.PositionDao;
import cn.zhh.admin.entity.Position;
import cn.zhh.common.enums.IsDeletedEnum;
import cn.zhh.common.enums.PositionSourceEnum;
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
    private PositionDao dao;

    @Override
    public JpaRepository<Position, Long> dao() {
        return dao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Position insert(Position entity) {
        // 判断是否已经存在，如果存在直接返回原数据
        Example<Position> positionExample = buildExample(Position.class, "source", entity.getSource(),
                "uniqueKey", entity.getUniqueKey(), "isDeleted", IsDeletedEnum.NO.getCode());
        Optional<Position> positionOptional = getByExample(positionExample);
        if (positionOptional.isPresent()) {
            log.info("职位已经存在，来源：{}，名称：{}", PositionSourceEnum.code2desc(entity.getSource()), entity.getName());
            return positionOptional.get();
        }
        // 插入数据库
        return save(entity);
    }
}
