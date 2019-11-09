package cn.zhh.crawler.recruit.society;

import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/9 16:09
 */
public interface PositionUrlCrawlService {

    Byte webSite();

    String baseUrl();

    void beforeProcess(WebDriver webDriver, Byte city, String position);

    List<Document> generateItems(Document document);

    WebElement nextPage(WebDriver webDriver);

    String parseUrl(String baseUrl, Document itemDocument);
}
