package cn.zhh.crawler.constant;

import cn.zhh.crawler.util.MapUtils;

import java.util.Map;

/**
 * 爬虫相关常量
 *
 * @author Zhou Huanghua
 */
public interface CrawlerConsts {

    String ZHILIAN_SEARCH_URL = "https://fe-api.zhaopin.com/c/i/sou";

    String LAGOU_SEARCH_URL = "https://www.lagou.com/jobs/positionAjax.json";

    Map<String, String> HEADER_MAP = MapUtils.buildMap("Origin", "https://sou.zhaopin.com",
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36",
            "Accept", "application/json, text/plain, */*", "Cookie", "token");

    String PAGE_SIZE = "250";
}
