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

    Map<String, String> HEADER_MAP = MapUtils.buildMap("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:54.0) Gecko/20100101 Firefox/54.0",
            "Accept", "*/*"
    );

    String PAGE_SIZE = String.valueOf(Runtime.getRuntime().availableProcessors());
}
