package cn.zhh.crawler.constant;

import java.util.concurrent.TimeUnit;

/**
 * 常量
 *
 * @author Zhou Huanghua
 */
public interface Consts {

    long BROWSER_OPENPAGE_TIMEOUT_VALUE = 10;

    TimeUnit BROWSER_OPENPAGE_TIMEOUT_UNIT = TimeUnit.SECONDS;

    String LINE_SEPARATOR = System.lineSeparator();

    String MQ_EXCHANGE_NAME = "project";

    String URL_QUEUE_NAME = "position_url_queue";

    String URL_ROUTING_KEY = "position_url";

    String DETAIL_QUEUE_NAME = "position_detail_queue";

    String DETAIL_ROUTING_KEY = "position_detail";

    String SYSTEM = "SYSTEM";
}
