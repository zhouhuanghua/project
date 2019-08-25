package cn.zhh.crawler.constant;

import java.util.concurrent.TimeUnit;

/**
 * 爬虫相关常量
 *
 * @author Zhou Huanghua
 */
public interface CrawlerConsts {

    /* 浏览器打开网页超时时间数值 */
    long BROWSER_OPENPAGE_TIMEOUT_VALUE = 10;

    /* 浏览器打开网页超时时间单位 */
    TimeUnit BROWSER_OPENPAGE_TIMEOUT_UNIT = TimeUnit.SECONDS;
}
