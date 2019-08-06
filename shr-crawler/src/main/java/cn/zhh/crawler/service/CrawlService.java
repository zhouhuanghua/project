package cn.zhh.crawler.service;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;

/**
 * 爬虫服务
 *
 * @author Zhou Huanghua
 */
public interface CrawlService {

    /**
     * 根据搜索条件爬取职位数据，并推送至MQ
     *
     * @param searchCondition 搜索条件
     * @throws Exception
     */
    void crawl(SearchPositionInfoMsg searchCondition) throws Exception;
}
