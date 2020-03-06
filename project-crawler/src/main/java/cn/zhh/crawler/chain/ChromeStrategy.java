package cn.zhh.crawler.chain;

import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.common.util.ThrowableUtils;
import cn.zhh.crawler.runner.BrowserDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;

/**
 * JBrowser策略
 *
 * @author Zhou Huanghua
 */
@Slf4j
public class ChromeStrategy implements IStrategy {

    @Override
    public void crawl(String url, ObjectWrapper<Document> docWrapper, CrawlStrategyChain strategyChain) {
        WebDriver webDriver = BrowserDriverFactory.openChromeBrowser();
        try {
            webDriver.get(url);
            Document document = Jsoup.parse(webDriver.getPageSource());
            if (isNormalPage(document)) {
                docWrapper.setObj(document);
                return;
            }
        } catch (Throwable t) {
            log.warn("Chrome加载网页[url={}]异常，t={}", url, ThrowableUtils.getStackTrace(t));
        } finally {
            OptionalOperationUtils.consumeIfNonNull(webDriver, WebDriver::quit);
        }
        strategyChain.doCrawl(url, docWrapper);
    }
}
