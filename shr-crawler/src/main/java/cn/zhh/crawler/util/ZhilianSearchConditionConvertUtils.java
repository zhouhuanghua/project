package cn.zhh.crawler.util;

import java.util.Map;

/**
 * 智联搜索条件转换工具
 *
 * @author Zhou Huanghua
 */
public class ZhilianSearchConditionConvertUtils {

    /* 北上广深、杭州、成都 */
    private static final Map<String, String> CITY_MAP = MapUtils.buildMap("北京", "530", "上海", "538",  "广州", "763", "深圳",
            "765", "杭州", "653", "成都", "801"
    );

    /* 无、1-3年、3-5年、5-10年、10年以上 */
    private static final Map<String, String> WORK_EXP_MAP = MapUtils.buildMap("无", "0000", "1-3年", "0103",  "3-5年", "0305",
            "5-10年", "0510", "10年以上", "1099"
    );

    /* 大专以下、大专、本科、硕士、博士 */
    private static final Map<String, String> EDUCATION_MAP = MapUtils.buildMap("大专以下", "7", "大专", "5",  "本科", "4", "硕士",
            "3", "博士", "1"
    );

    /* 未融资、天使轮、A轮、B轮、C轮、D轮及以上、上市公司、不需要融资 */
    private static final Map<String, String> COMPANY_SCALE_MAP = MapUtils.buildMap("未融资", "", "天使轮", "",  "A轮", "", "B轮", "", "C轮", "", "D轮及以上", "",
            "上市公司", "", "不需要融资", ""
    );

    public static String getCity(String name) {
        return CITY_MAP.getOrDefault(name, "-1");
    }

    public static String getworkExp(String name) {
        return WORK_EXP_MAP.getOrDefault(name, "-1");
    }

    public static String getEducation(String name) {
        return EDUCATION_MAP.getOrDefault(name, "-1");
    }

    public static String getCompanyScale(String name) {
        return COMPANY_SCALE_MAP.getOrDefault(name, "-1");
    }
}
