package cn.zhh.crawler.service.crawl;

import cn.zhh.common.enums.CityEnum;
import cn.zhh.common.enums.EducationEnum;
import cn.zhh.common.enums.WorkExpEnum;
import cn.zhh.crawler.util.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
class ZhilianBossSearchConditionConverter {

    static enum SITE_NAME {
        ZHILIAN, BOSS
    }

    private static final String ZHILIAN = SITE_NAME.ZHILIAN.name();
    private static final String BOSS = SITE_NAME.BOSS.name();

    private static Map<Byte, Map<String, String>> CITY_MAP = new HashMap<>(8);

    private static Map<Byte, Map<String, String>> WORK_EXP_MAP = new HashMap<>(8);

    private static Map<Byte, Map<String, String>> EDUCATION_MAP = new HashMap<>(8);

    // 初始化数据
    static {
        // 城市
        CITY_MAP.put(CityEnum.ALL.getCode(), MapUtils.buildMap(ZHILIAN, "-1", BOSS, "100010000"));
        CITY_MAP.put(CityEnum.BEIJING.getCode(), MapUtils.buildMap(ZHILIAN, "530", BOSS, "101010100"));
        CITY_MAP.put(CityEnum.SHANGHAI.getCode(), MapUtils.buildMap(ZHILIAN, "538", BOSS, "101020100"));
        CITY_MAP.put(CityEnum.GUANGZHOU.getCode(), MapUtils.buildMap(ZHILIAN, "763", BOSS, "101280100"));
        CITY_MAP.put(CityEnum.SHENZHEN.getCode(), MapUtils.buildMap(ZHILIAN, "765", BOSS, "101280600"));
        CITY_MAP.put(CityEnum.HANGZHOU.getCode(), MapUtils.buildMap(ZHILIAN, "653", BOSS, "101210100"));
        CITY_MAP.put(CityEnum.CHENGDU.getCode(), MapUtils.buildMap(ZHILIAN, "801", BOSS, "101270100"));

        // 工作经验(只有智联)
        WORK_EXP_MAP.put(WorkExpEnum.ALL.getCode(), MapUtils.buildMap(ZHILIAN, "-1", BOSS, ""));
        WORK_EXP_MAP.put(WorkExpEnum.NONE.getCode(), MapUtils.buildMap(ZHILIAN, "0000", BOSS, ""));
        WORK_EXP_MAP.put(WorkExpEnum.ONE2THREE.getCode(), MapUtils.buildMap(ZHILIAN, "0103", BOSS, ""));
        WORK_EXP_MAP.put(WorkExpEnum.THREE2FIVE.getCode(), MapUtils.buildMap(ZHILIAN, "0305", BOSS, ""));
        WORK_EXP_MAP.put(WorkExpEnum.FIVE2TEN.getCode(), MapUtils.buildMap(ZHILIAN, "0510", BOSS, ""));
        WORK_EXP_MAP.put(WorkExpEnum.MORE10.getCode(), MapUtils.buildMap(ZHILIAN, "1099", BOSS, ""));
        WORK_EXP_MAP.put(WorkExpEnum.NOT_REQUIRED.getCode(), MapUtils.buildMap(ZHILIAN, "-1", BOSS, ""));

        // 学历(只有智联)
        EDUCATION_MAP.put(EducationEnum.ALL.getCode(), MapUtils.buildMap(ZHILIAN, "-1", BOSS, ""));
        EDUCATION_MAP.put(EducationEnum.JUNIOR_COLLEGE.getCode(), MapUtils.buildMap(ZHILIAN, "5", BOSS, ""));
        EDUCATION_MAP.put(EducationEnum.UNDERGRADUATE.getCode(), MapUtils.buildMap(ZHILIAN, "4", BOSS, ""));
        EDUCATION_MAP.put(EducationEnum.MASTER.getCode(), MapUtils.buildMap(ZHILIAN, "3", BOSS, ""));
        EDUCATION_MAP.put(EducationEnum.DOCTOR.getCode(), MapUtils.buildMap(ZHILIAN, "1", BOSS, ""));
        EDUCATION_MAP.put(EducationEnum.NOT_REQUIRED.getCode(), MapUtils.buildMap(ZHILIAN, "8", BOSS, ""));
    }

    static String getCity(Byte city, SITE_NAME siteNameEnum) {
        if (Objects.isNull(city) || !CITY_MAP.containsKey(city)) {
            return CITY_MAP.get(CityEnum.ALL.getCode()).get(siteNameEnum.name());
        }
        return CITY_MAP.get(city).get(siteNameEnum);
    }

    static String getWorkExpForZhilian(Byte workExp) {
        if (Objects.isNull(workExp) || !WORK_EXP_MAP.containsKey(workExp)) {
            return WORK_EXP_MAP.get(WorkExpEnum.ALL.getCode()).get(SITE_NAME.ZHILIAN.name());
        }
        return WORK_EXP_MAP.get(workExp).get(SITE_NAME.ZHILIAN.name());
    }

    static String getEducationForZhilian(Byte education) {
        if (Objects.isNull(education) || !EDUCATION_MAP.containsKey(education)) {
            return EDUCATION_MAP.get(EducationEnum.ALL.getCode()).get(SITE_NAME.ZHILIAN.name());
        }
        return EDUCATION_MAP.get(education).get(SITE_NAME.ZHILIAN.name());
    }
}
