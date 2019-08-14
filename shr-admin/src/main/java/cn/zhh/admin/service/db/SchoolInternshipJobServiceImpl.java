package cn.zhh.admin.service.db;

import cn.zhh.admin.dao.db.SchoolInternshipJobDao;
import cn.zhh.admin.entity.SchoolInternshipJob;
import cn.zhh.common.enums.IsDeletedEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 校招-实习-岗位服务
 *
 * @author Zhou Huanghua
 */
@Service
@Slf4j
public class SchoolInternshipJobServiceImpl implements SchoolInternshipJobService {
    @Autowired
    private SchoolInternshipJobDao schoolInternshipJobDao;

    @Override
    public JpaRepository<SchoolInternshipJob, Long> dao() {
        return schoolInternshipJobDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchoolInternshipJob insert(SchoolInternshipJob entity) {
        // 判断是否已经存在，如果存在则更新，否则插入
        Example<SchoolInternshipJob> jobExample = buildExample(SchoolInternshipJob.class,
                "uniqueKey", entity.getUniqueKey(), "isDeleted", IsDeletedEnum.NO.getCode());
        Optional<SchoolInternshipJob> jobOptional = getByExample(jobExample);
        if (jobOptional.isPresent()) {
            log.info("校招-实习-岗位{}已经存在！", entity.getUniqueKey());
            entity.setId(jobOptional.get().getId());
        }
        // 长度截取
        String introduce = entity.getIntroduce();
        if (StringUtils.hasText(introduce) && introduce.length() > 3000) {
            entity.setIntroduce(introduce.substring(0, 2800) + "......更多详情请查看公司官网。");
        }
        String description = entity.getDescription();
        if (StringUtils.hasText(description) && description.length() > 3000) {
            entity.setDescription(description.substring(0, 2800) + "......更多详情请查看公司官网。");
        }

        return save(entity);
    }
}
