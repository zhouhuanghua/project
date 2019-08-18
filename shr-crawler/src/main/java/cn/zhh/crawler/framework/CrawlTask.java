package cn.zhh.crawler.framework;

import cn.zhh.common.util.ThrowableUtils;
import cn.zhh.crawler.service.WebDriverFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 爬虫任务
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Setter
public class CrawlTask<T1, T2> {

    private T1 parameter;

    private WebDriverFactory webDriverFactory;

    private String baseUrl;

    private ListPageParser<T1> listPageParser;

    private int pageMax;

    private DetailPageParser<T2> detailPageParser;

    private int retryCount;

    private int retryWaitSecond;

    private CrawlTask() {
    }

    public static <T1, T2> CrawlTask<T1, T2> newInstance(T1 parameter,
                                                         WebDriverFactory webDriverFactory,
                                                         String baseUrl,
                                                         ListPageParser listPageParser,
                                                         int pageMax,
                                                         DetailPageParser<T2> detailPageParser,
                                                         int retryCount,
                                                         int retryWaitSecond) {
        CrawlTask<T1, T2> crawlTask = new CrawlTask<T1, T2>();
        crawlTask.setParameter(parameter);
        crawlTask.setWebDriverFactory(webDriverFactory);
        crawlTask.setBaseUrl(baseUrl);
        crawlTask.setListPageParser(listPageParser);
        crawlTask.setPageMax(pageMax);
        crawlTask.setDetailPageParser(detailPageParser);
        crawlTask.setRetryCount(retryCount);
        crawlTask.setRetryWaitSecond(retryWaitSecond);
        return crawlTask;
    }

    public void start() throws IOException {
        // 打开浏览器并跳转到指定页面
        WebDriver webDriver = webDriverFactory.openBrowser();
        try {
            process(webDriver);
        } finally {
            webDriver.quit();
        }
    }

    private void process(WebDriver webDriver) {
        webDriver.manage().window().maximize();
        log.info("【爬虫任务】开始跳转页面 {} ...", baseUrl);
        webDriver.navigate().to(baseUrl);
        sleep(5, TimeUnit.SECONDS);

        // 执行前置处理
        log.info("【爬虫任务】开始前置处理...");
        listPageParser.beforeProcess(webDriver, parameter);
        sleep(5, TimeUnit.SECONDS);

        // 分页抓取所有子项
        log.info("【爬虫任务】开始分页爬取列表数据项...");
        List<Document> itemDocumentList = getAllItemByPage(webDriver);
        log.info("【爬虫任务】一共爬到{}条列表数据项！", itemDocumentList.size());

        // 处理子项
        int i = 1;
        for (Document itemDocument : itemDocumentList) {
            try {
                log.info("【爬虫任务】开始处理第{}/{}条子项数据...", i++, itemDocumentList.size());
                processItem(itemDocument);
                sleep(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.info("处理子项异常！doc={}，e={}", itemDocument, ThrowableUtils.getThrowableStackTrace(e));
            }
        }
        log.info("【爬虫任务】任务结束！baseUrl={}，parameter={}", baseUrl, parameter);
    }

    private List<Document> getAllItemByPage(WebDriver webDriver) {
        List<Document> itemDocumentList = new ArrayList<>();
        for (int pageNum = 1; pageNum <= pageMax; ++pageNum) {
            Document document = Jsoup.parse(webDriver.getPageSource());
            itemDocumentList.addAll(listPageParser.generateItems(document));
            WebElement nextPageElement = listPageParser.nextPage(webDriver);
            if (Objects.isNull(nextPageElement)) {
                break;
            }
            nextPageElement.click();
            sleep(5, TimeUnit.SECONDS);
        }
        return itemDocumentList;
    }

    private void processItem(Document itemDocument) throws Exception {
        String detailUrl = detailPageParser.parseUrl(baseUrl, itemDocument);
        for (int count = 0; ; ++count) {
            Document detailDocument = Jsoup.connect(detailUrl).get();
            if (detailPageParser.isNormalPage(detailDocument)) {
                T2 obj = detailPageParser.generateObj(detailUrl, detailDocument);
                detailPageParser.processObj(obj);
                break;
            }
            if (count < retryCount) {
                log.info("页面异常，等待{}秒后重试", retryWaitSecond);
                sleep(retryWaitSecond, TimeUnit.SECONDS);
            } else {
                break;
            }
        }
    }

    public void sleep(long timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            log.warn("睡眠异常！", e);
        }
    }
}
