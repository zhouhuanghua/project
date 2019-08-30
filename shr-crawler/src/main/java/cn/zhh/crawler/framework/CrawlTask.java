package cn.zhh.crawler.framework;

import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.common.util.ThrowableUtils;
import cn.zhh.crawler.constant.CrawlerConsts;
import cn.zhh.crawler.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 爬虫任务
 *
 * @author Zhou Huanghua
 */
@Slf4j
public class CrawlTask<T1, T2> {

    private static final ExecutorService SELENIUM_EXECUTOR = Executors.newFixedThreadPool(2);

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(10);

    private static final Map<String, String> COMMON_HEADER_MAP = MapUtils.buildMap(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36",
            "Accept", "application/json, text/plain, */*", "Cookie", "token");

    private T1 parameter;

    private String baseUrl;

    private ListPageParser<T1> listPageParser;

    private int pageMax;

    private DetailPageParser<T2> detailPageParser;

    private int retryCount;

    private int retryWaitSecond;

    private CrawlTask() {
    }

    public static <T1, T2> CrawlTask<T1, T2> newInstance(T1 parameter,
                                                         String baseUrl,
                                                         ListPageParser listPageParser,
                                                         int pageMax,
                                                         DetailPageParser<T2> detailPageParser,
                                                         int retryCount,
                                                         int retryWaitSecond) {
        CrawlTask<T1, T2> crawlTask = new CrawlTask<T1, T2>();
        crawlTask.parameter = parameter;
        crawlTask.baseUrl = baseUrl;
        crawlTask.listPageParser = listPageParser;
        crawlTask.pageMax = pageMax;
        crawlTask.detailPageParser = detailPageParser;
        crawlTask.retryCount = retryCount;
        crawlTask.retryWaitSecond = retryWaitSecond;
        return crawlTask;
    }

    public void start() throws IOException {
        log.info("【爬虫任务】启动...");
        WebDriver webDriver = BrowserDriverFactory.openBrowser(BrowserDriverFactory.DriverType.CHROME);
        try {
            process(webDriver);
            log.info("【爬虫任务】任务结束！baseUrl={}，parameter={}", baseUrl, parameter);
        } catch (Exception e) {
            log.info("【爬虫任务】执行异常！e={}", ThrowableUtils.getThrowableStackTrace(e));
        } finally {
            webDriver.quit();
        }
    }

    private void process(WebDriver webDriver) {
        webDriver.manage().window().maximize();
        log.info("【爬虫任务】开始跳转页面{}...", baseUrl);
        openPage(webDriver, baseUrl);
        sleep(1, TimeUnit.SECONDS);

        // 执行前置处理
        log.info("【爬虫任务】开始前置处理...");
        listPageParser.beforeProcess(webDriver, parameter);
        sleep(1, TimeUnit.SECONDS);

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
            } catch (Exception e) {
                log.info("【爬虫任务】处理第{}/{}条子项数据异常！doc={}，e={}", i, itemDocumentList.size(),
                        itemDocument, ThrowableUtils.getThrowableStackTrace(e));
            }
        }
    }

    private void openPage(WebDriver driver, String url) {
        ScheduledFuture<?> scheduledFuture = SCHEDULED_EXECUTOR.schedule(() -> {
            ((JavascriptExecutor) driver).executeScript("window.stop();");
        }, Double.valueOf(CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_VALUE * 0.8).longValue(), CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_UNIT);
        driver.get(url);
        scheduledFuture.cancel(false);
    }

    private List<Document> getAllItemByPage(WebDriver webDriver) {
        List<Document> itemDocumentList = new ArrayList<>();
        for (int pageNum = 1; pageNum <= pageMax; ++pageNum) {
            log.info("【爬虫任务】正在获取第{}页子项数据...", pageNum);
            Document document = Jsoup.parse(webDriver.getPageSource());
            itemDocumentList.addAll(listPageParser.generateItems(document));
            WebElement nextPageElement = listPageParser.nextPage(webDriver);
            if (Objects.isNull(nextPageElement)) {
                break;
            }
            nextPageElement.click();
            sleep(1, TimeUnit.SECONDS);
        }
        return itemDocumentList;
    }

    private void processItem(Document itemDocument) throws Exception {
        String detailUrl = detailPageParser.parseUrl(baseUrl, itemDocument);
        for (int count = 0; ; ++count) {
            if (Objects.isNull(detailUrl)) {
                continue;
            }
            Document detailDocument = Jsoup.connect(detailUrl).headers(getCommonHeaderMap(detailUrl)).get();
            if (detailPageParser.isNormalPage(detailDocument)) {
                T2 obj = detailPageParser.generateObj(detailUrl, detailDocument);
                detailPageParser.processObj(obj);
                log.info("【爬虫任务】处理页面{}成功！", detailUrl);
                break;
            }
            if (count < retryCount) {
                log.info("【爬虫任务】页面{}异常，等待{}秒后重试！", detailUrl, retryWaitSecond);
                sleep(retryWaitSecond, TimeUnit.SECONDS);
            } else {
                log.info("【爬虫任务】页面{}加入Selenium处理队列！", detailUrl, retryWaitSecond);
                SELENIUM_EXECUTOR.execute(() -> this.seleniumHandle(detailUrl));
                break;
            }
        }
    }

    private void seleniumHandle(String detailPageUrl) {
        WebDriver webDriver = BrowserDriverFactory.openBrowser(BrowserDriverFactory.DriverType.CHROME);
        try {
            webDriver.manage().window().maximize();
            log.info("【爬虫任务-Selenium】开始处理页面{}...", detailPageUrl);
            openPage(webDriver, detailPageUrl);
            CrawlTask.this.sleep(1, TimeUnit.SECONDS);
            Document detailDocument = Jsoup.parse(webDriver.getPageSource());
            if (detailPageParser.isNormalPage(detailDocument)) {
                T2 obj = detailPageParser.generateObj(detailPageUrl, detailDocument);
                detailPageParser.processObj(obj);
                log.info("【爬虫任务-Selenium】处理页面{}成功！", detailPageUrl);
            } else {
                log.info("【爬虫任务-Selenium】处理页面{}失败！", detailPageUrl);
            }
        } catch (Exception e) {
            log.error("【爬虫任务-Selenium】处理页面{}异常，e={}", detailPageUrl, ThrowableUtils.getThrowableStackTrace(e));
        } finally {
            OptionalOperationUtils.consumeIfNonNull(webDriver, WebDriver::quit);
        }
    }

    private void sleep(long timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            log.warn("睡眠异常！", e);
        }
    }

    private Map<String, String> getCommonHeaderMap(String origin) {
        Map<String, String> headerMap = MapUtils.buildMap("Origin", origin);
        headerMap.putAll(COMMON_HEADER_MAP);
        return headerMap;
    }
}
