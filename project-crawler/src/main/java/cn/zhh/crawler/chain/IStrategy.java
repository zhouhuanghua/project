package cn.zhh.crawler.chain;

import org.jsoup.nodes.Document;

import java.util.Objects;

/**
 * 策略
 *
 * @author Zhou Huanghua
 * @date 2020/3/6 22:03
 */
public interface IStrategy {

    default boolean isNormalPage(Document document) {
        return Objects.nonNull(document.selectFirst("div[class=job-name]"));
    }

    void crawl(String url, ObjectWrapper<Document> docWrapper, CrawlStrategyChain strategyChain);
}
