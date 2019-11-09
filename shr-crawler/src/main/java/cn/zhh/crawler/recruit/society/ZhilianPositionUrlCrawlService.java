package cn.zhh.crawler.recruit.society;

import cn.zhh.common.enums.CityEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/9 17:18
 */
@Component
public class ZhilianPositionUrlCrawlService implements PositionUrlCrawlService {

    @Override
    public Byte webSite() {
        return PositionSourceEnum.ZHILIAN.getCode();
    }

    @Override
    public String baseUrl() {
        return "https://www.zhaopin.com";
    }

    @Override
    public void beforeProcess(WebDriver webDriver, Byte city, String position) {
        // 关闭弹窗
        webDriver.findElement(By.cssSelector("div[class=risk-warning__content]"))
                .findElement(By.tagName("button")).click();
        // 搜索
        webDriver.findElement(By.cssSelector("input[class=zp-search__input]")).sendKeys(position);
        webDriver.findElement(By.cssSelector("a[class='zp-search__btn zp-search__btn--blue']")).click();

        SleepUtils.sleepSeconds(1);

        // 选择城市
        Iterator<String> iterator = webDriver.getWindowHandles().iterator();
        while (iterator.hasNext()) {
            String window = iterator.next();
            if (!Objects.equals(window, webDriver.getWindowHandle())) {
                webDriver.switchTo().window(window);
                break;
            }
        }
        webDriver.findElement(By.id("queryTitleUls"))
                .findElement(By.cssSelector("li[class='currentCity query-city__uls__li current-city']"))
                .findElement(By.cssSelector("span[class='current-city__down span_down']")).click();
        SleepUtils.sleepSeconds(1);
        List<WebElement> liElementList = webDriver.findElement(By.id("queryCityBox"))
                .findElement(By.cssSelector("div[class='cityChild city-child']"))
                .findElement(By.cssSelector("ul[class='choiseCity clearfix city-child__choise']"))
                .findElements(By.tagName("li"));
        Map<Byte, WebElement> cityMap = new HashMap<>(8);
        for (WebElement liElement : liElementList) {
            String text = liElement.getText();
            switch (text) {
                case "北京":
                    cityMap.put(CityEnum.BEIJING.getCode(), liElement);
                    break;
                case "上海":
                    cityMap.put(CityEnum.SHANGHAI.getCode(), liElement);
                    break;
                case "广州":
                    cityMap.put(CityEnum.GUANGZHOU.getCode(), liElement);
                    break;
                case "深圳":
                    cityMap.put(CityEnum.SHENZHEN.getCode(), liElement);
                    break;
                case "成都":
                    cityMap.put(CityEnum.HANGZHOU.getCode(), liElement);
                    break;
                case "杭州":
                    cityMap.put(CityEnum.CHENGDU.getCode(), liElement);
                    break;
                default:
                    break;
            }
        }
        if (Objects.nonNull(city) || cityMap.containsKey(city)) {
            WebElement element = cityMap.get(city);
            element.findElement(By.tagName("a")).click();
        }

        SleepUtils.sleepSeconds(1);

        // 点击按最新发布排序
        webDriver.findElement(By.id("sou_sortUls")).findElements(By.tagName("li")).get(2).click();
    }

    @Override
    public List<Document> generateItems(Document document) {
        Elements elements = document.getElementById("listContent")
                .select("div[class='contentpile__content__wrapper clearfix']");
        return elements.stream().map(e -> Jsoup.parse(e.html())).collect(Collectors.toList());
    }

    @Override
    public WebElement nextPage(WebDriver webDriver) {
        new Actions(webDriver).sendKeys(Keys.END).perform();
        SleepUtils.sleepSeconds(1);
        return webDriver.findElement(By.id("pagination_content"))
                .findElement(By.cssSelector("button[class='btn soupager__btn']"));
    }

    @Override
    public String parseUrl(String baseUrl, Document itemDocument) {
        return itemDocument.selectFirst("a[class=contentpile__content__wrapper__item__info]")
                .attr("href");
    }
}
