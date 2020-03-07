package cn.zhh.crawler.runner;

import cn.zhh.common.constant.CityEnum;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.common.util.ThrowableUtils;
import cn.zhh.crawler.constant.CrawlerConsts;
import cn.zhh.crawler.dto.UrlDTO;
import cn.zhh.crawler.mq.MqProducer;
import cn.zhh.crawler.util.SleepUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 爬取链接执行器
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class CrawlUrlRunner {

    @Autowired
    private MqProducer mqProducer;

    public void crawlUrl(CityEnum cityEnum, String position, Integer pageNum) {
        WebDriver webDriver = BrowserDriverFactory.openChromeBrowser();
        try {
            webDriver.manage().window().maximize();
            webDriver.get(CrawlerConsts.LAGOU_BASE_URL);
            selectCity(webDriver, cityEnum);
            searchPosition(webDriver, position);
            for (int index = 0; index < pageNum; pageNum++) {
                List<Document> itemDocumentList = generateItems(Jsoup.parse(webDriver.getPageSource()));
                for (Document itemDocument : itemDocumentList) {
                    String detailUrl = itemDocument.selectFirst("a[class=position_link]").attr("href");
                    mqProducer.sendUrl(new UrlDTO(detailUrl, CrawlerConsts.URL_RETRY_COUNT));
                }
                WebElement nextPageElement;
                if (Objects.equals(index, pageNum - 1) || Objects.isNull(nextPageElement = nextPage(webDriver))) {
                    break;
                }
                nextPageElement.click();
                SleepUtils.sleepSeconds(3);
            }
        } catch (Throwable t) {
            log.error("爬取链接异常，t={}", ThrowableUtils.getStackTrace(t));
        } finally {
            OptionalOperationUtils.consumeIfNonNull(webDriver, WebDriver::quit);
        }
    }

    private void selectCity(WebDriver webDriver, CityEnum city) {
        SleepUtils.sleepSeconds(1);
        List<WebElement> aElements = webDriver.findElement(By.id("changeCityBox")).findElements(By.tagName("a"));
        String cityName = city.getDesc();
        for (WebElement aElement : aElements) {
            if (aElement.getText().contains(cityName)) {
                aElement.click();
                return;
            }
        }
        webDriver.findElement(By.id("cboxClose")).click();
    }

    private void searchPosition(WebDriver webDriver, String position) {
        // 搜索
        webDriver.findElement(By.id("search_input")).sendKeys(position);
        webDriver.findElement(By.id("search_button")).click();
        SleepUtils.sleepSeconds(1);
        // 关闭领红包窗口
        WebElement divElement = webDriver.findElement(By.cssSelector("div[class=body-btn]"));
        OptionalOperationUtils.consumeIfNonNull(divElement, WebElement::click);
        // 按最新排序
        OptionalOperationUtils.consumeIfNonNull(webDriver.findElement(By.id("order")), orderElement -> {
            orderElement.findElement(By.cssSelector("li[class=wrapper]"))
                    .findElement(By.tagName("div")).findElements(By.tagName("a")).get(1).click();
        });
        SleepUtils.sleepSeconds(1);
    }

    private List<Document> generateItems(Document document) {
        Elements elements = document.getElementById("s_position_list").selectFirst("ul[class=item_con_list]").getElementsByTag("li");
        return elements.stream().map(e -> Jsoup.parse(e.html())).collect(Collectors.toList());
    }

    private WebElement nextPage(WebDriver webDriver) {
        return webDriver.findElement(By.className("pager_next"));
    }
}
