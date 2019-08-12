package cn.zhh.crawler.service.crawl;

import cn.zhh.common.enums.CityEnum;
import cn.zhh.crawler.util.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 搜索条件转换器
 *
 * @author Zhou Huanghua
 */
class SearchConditionConverter {

    static enum SITE_NAME {
        ZHILIAN, BOSS
    }

    private static final String ZHILIAN = SITE_NAME.ZHILIAN.name();
    private static final String BOSS = SITE_NAME.BOSS.name();

    private static Map<Byte, Map<String, String>> CITY_MAP = new HashMap<>(8);

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
    }

    static String getCity(Byte city, SITE_NAME siteNameEnum) {
        if (Objects.isNull(city) || !CITY_MAP.containsKey(city)) {
            return CITY_MAP.get(CityEnum.ALL.getCode()).get(siteNameEnum.name());
        }
        return CITY_MAP.get(city).get(siteNameEnum);
    }
}
