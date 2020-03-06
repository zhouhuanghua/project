package cn.zhh.crawler.chain;

import cn.zhh.common.util.MapUtils;
import cn.zhh.common.util.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

/**
 * Jsoup策略
 *
 * @author Zhou Huanghua
 * @date 2020/3/6 22:30
 */
@Slf4j
public class JsoupStrategy implements IStrategy {

    private final Map<String, String> COMMON_HEADER_MAP = MapUtils.buildMap(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36",
            "Accept", "application/json, text/plain, */*",
            "Cookie", "token");

    @Override
    public void crawl(String url, ObjectWrapper<Document> docWrapper, CrawlStrategyChain strategyChain) {
        try {
            Document document = Jsoup.connect(url).headers(COMMON_HEADER_MAP).get();
            if (isNormalPage(document)) {
                docWrapper.setObj(document);
                return;
            }
        } catch (Throwable t) {
            log.warn("Jsoup加载网页[url={}]异常，t={}", url, ThrowableUtils.getStackTrace(t));
        }
        strategyChain.doCrawl(url, docWrapper);
    }
}
