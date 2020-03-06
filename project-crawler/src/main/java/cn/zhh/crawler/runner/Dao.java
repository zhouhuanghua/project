package cn.zhh.crawler.runner;

import cn.zhh.common.constant.Consts;
import cn.zhh.common.dto.PositionInfo;
import cn.zhh.common.util.ThrowableUtils;
import cn.zhh.crawler.util.SimpleBloomFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * 数据访问对象
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Repository
public class Dao {

    private static final SimpleBloomFilter BLOOM_FILTER = new SimpleBloomFilter();

    private static final String COUTN_SQL = "select count(*) from inf_position where unique_key = ? and is_deleted = 0";

    private static final String INSERT_SQL = "insert into inf_position (" +
            "unique_key, name, salary, city, work_exp, education, welfare, description, label, work_address," +
            "publish_time, url, company_name, company_logo, company_developmental_stage," +
            "company_scale, company_domain, company_url, company_introduction, creator" +
            ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertDb(PositionInfo positionInfo) {
        String uniqueKey = positionInfo.getUniqueKey();
        if (BLOOM_FILTER.contains(uniqueKey)
                && (jdbcTemplate.queryForObject(COUTN_SQL, new Object[]{uniqueKey}, Integer.class) > 0)) {
            log.info("职位已存在，uniqueKey={}", uniqueKey);
            return;
        }
        setDefValIfNull(positionInfo);
        Object[] args = {
                uniqueKey,
                positionInfo.getName(),
                positionInfo.getSalary(),
                positionInfo.getCity(),
                positionInfo.getWorkExp(),
                positionInfo.getEducation(),
                positionInfo.getWelfare(),
                positionInfo.getDescription(),
                positionInfo.getLabel(),
                positionInfo.getWorkAddress(),
                positionInfo.getPublishTime(),
                positionInfo.getUrl(),
                positionInfo.getCompanyName(),
                positionInfo.getCompanyLogo(),
                positionInfo.getCompanyDevelopmentalStage(),
                positionInfo.getCompanyScale(),
                positionInfo.getCompanyDomain(),
                positionInfo.getCompanyUrl(),
                positionInfo.getCompanyIntroduction(),
                Consts.SYSTEM
        };
        try {
            jdbcTemplate.update(INSERT_SQL, args);
        } catch (DuplicateKeyException t) {
            log.info("唯一键冲突，uniqueKey={}", uniqueKey);
        }
        BLOOM_FILTER.add(uniqueKey);
    }

    private void setDefValIfNull(PositionInfo positionInfo) {
        Class<? extends PositionInfo> clazz = positionInfo.getClass();
        Arrays.stream(clazz.getDeclaredFields()).filter(f -> Modifier.isPrivate(f.getModifiers()) && !Modifier.isStatic(f.getModifiers()))
                .forEach(f -> {
                    try {
                        f.setAccessible(true);
                        Object value = f.get(positionInfo);
                        if (Objects.nonNull(value)) {
                            return;
                        }
                        Class<?> type = f.getType();
                        if (String.class.isAssignableFrom(type)) {
                            f.set(positionInfo, "");
                        } else if (Date.class.isAssignableFrom(type)) {
                            f.set(positionInfo, new Date());
                        } else if (Number.class.isAssignableFrom(type)) {
                            f.set(positionInfo, 0);
                        }
                    } catch (Throwable t) {
                        log.error("反射修改对象空值异常，t={}", ThrowableUtils.getStackTrace(t));
                    }
                });
    }
}
