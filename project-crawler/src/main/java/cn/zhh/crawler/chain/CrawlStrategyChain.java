package cn.zhh.crawler.chain;

import org.jsoup.nodes.Document;

import java.util.Objects;

public final class CrawlStrategyChain {
    private int pos = 0;
    private int n;
    private IStrategy[] strategies;

    private CrawlStrategyChain() {
    }

    public static CrawlStrategyChain build(IStrategy[] strategies) {
        CrawlStrategyChain instance = new CrawlStrategyChain();
        instance.strategies = Objects.requireNonNull(strategies, "CrawlStrategyChain构造参数strategies不能为空！");
        instance.n = strategies.length;
        return instance;
    }

    public void doCrawl(String url, ObjectWrapper<Document> docWrapper) {
        if (pos < n) {
            strategies[pos++].crawl(url, docWrapper, this);
        }
    }
}