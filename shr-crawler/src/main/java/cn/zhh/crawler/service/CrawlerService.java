package cn.zhh.crawler.service;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;

/**
 * 爬虫服务
 *
 * @author Zhou Huanghua
 */
public interface CrawlerService {

    void crawl(SearchPositionInfoMsg searchCondition) throws Exception;
}
