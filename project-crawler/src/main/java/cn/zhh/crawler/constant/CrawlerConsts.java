package cn.zhh.crawler.constant;

import java.util.concurrent.TimeUnit;

/**
 * 常量
 *
 * @author Zhou Huanghua
 */
public interface CrawlerConsts {

    String LAGOU_BASE_URL = "https://www.lagou.com/";

    long BROWSER_OPENPAGE_TIMEOUT_VALUE = 10;

    TimeUnit BROWSER_OPENPAGE_TIMEOUT_UNIT = TimeUnit.SECONDS;

    String MQ_EXCHANGE_NAME = "project";

    String URL_QUEUE_NAME = "position_url_queue";

    String URL_ROUTING_KEY = "position_url";

    String DETAIL_QUEUE_NAME = "position_detail_queue";

    String DETAIL_ROUTING_KEY = "position_detail";

    Integer URL_RETRY_COUNT = 3;
}
