package cn.zhh.crawler.framework;

import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 列表页面解析器
 *
 * @author Zhou Huanghua
 */
public interface ListPageParser<T> {

    void beforeProcess(WebDriver webDriver, T parameter);

    List<Document> generateItems(Document document);

    WebElement nextPage(WebDriver webDriver);

    default void sleep(long timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            // 忽略
        }
    }
}
