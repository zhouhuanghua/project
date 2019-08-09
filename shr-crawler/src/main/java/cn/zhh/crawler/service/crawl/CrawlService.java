package cn.zhh.crawler.service.crawl;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import com.rabbitmq.client.Channel;
import org.springframework.messaging.handler.annotation.Headers;

import java.util.Map;

/**
 * 爬虫服务
 *
 * @author Zhou Huanghua
 */
public interface CrawlService {

    /**
     * 根据搜索条件爬取职位数据，并推送至MQ
     *
     * @param searchCondition 职位搜索条件
     * @throws Exception
     */
    void crawl(SearchPositionInfoMsg searchCondition) throws Exception;

    void consumeMq(SearchPositionInfoMsg searchCondition, @Headers Map<String, Object> headers, Channel channel) throws Exception;
}
