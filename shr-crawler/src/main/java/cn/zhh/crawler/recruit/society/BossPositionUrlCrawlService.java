package cn.zhh.crawler.recruit.society;

import cn.zhh.common.enums.CityEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/9 17:18
 */
@Component
public class BossPositionUrlCrawlService implements PositionUrlCrawlService {

    @Override
    public Byte webSite() {
        return PositionSourceEnum.BOSS.getCode();
    }

    @Override
    public String baseUrl() {
        return "https://www.zhipin.com";
    }

    @Override
    public void beforeProcess(WebDriver webDriver, Byte city, String position) {
        // 选择城市
        webDriver.findElement(By.cssSelector("span[class=switchover-city]")).click();
        List<WebElement> liElements = webDriver.findElement(By.cssSelector("ul[class=dorpdown-province]")).findElements(By.tagName("li"));
        Actions actions = new Actions(webDriver);
        actions.moveToElement(liElements.get(0)).perform();
        List<WebElement> liElementList = webDriver.findElement(By.cssSelector("div[class=dorpdown-city]"))
                .findElement(By.cssSelector("ul[class=show]")).findElements(By.tagName("li"));
        Map<Byte, WebElement> cityMap = new HashMap<>(8);
        for (WebElement liElement : liElementList) {
            String text = liElement.getAttribute("ka");
            switch (text) {
                case "hot-city-100010000":
                    cityMap.put(CityEnum.ALL.getCode(), liElement);
                    break;
                case "hot-city-101010100":
                    cityMap.put(CityEnum.BEIJING.getCode(), liElement);
                    break;
                case "hot-city-101020100":
                    cityMap.put(CityEnum.SHANGHAI.getCode(), liElement);
                    break;
                case "hot-city-101280100":
                    cityMap.put(CityEnum.GUANGZHOU.getCode(), liElement);
                    break;
                case "hot-city-101280600":
                    cityMap.put(CityEnum.SHENZHEN.getCode(), liElement);
                    break;
                case "hot-city-101210100":
                    cityMap.put(CityEnum.HANGZHOU.getCode(), liElement);
                    break;
                case "hot-city-101270100":
                    cityMap.put(CityEnum.CHENGDU.getCode(), liElement);
                    break;
                default:
                    break;
            }
        }
        if (Objects.isNull(city) || !cityMap.containsKey(city)) {
            actions.moveToElement(cityMap.get(CityEnum.ALL.getCode())).click().perform();
        }
        actions.moveToElement(cityMap.get(city)).click().perform();

        SleepUtils.sleepSeconds(1);

        // 搜索
        webDriver.findElement(By.cssSelector("input[class=ipt-search]")).sendKeys(position);
        webDriver.findElement(By.cssSelector("button[class='btn btn-search']")).click();
    }

    @Override
    public List<Document> generateItems(Document document) {
        Elements elements = document.selectFirst("div[class=job-list]").selectFirst("ul").getElementsByTag("li");
        return elements.stream().map(e -> Jsoup.parse(e.html())).collect(Collectors.toList());
    }

    @Override
    public WebElement nextPage(WebDriver webDriver) {
        new Actions(webDriver).sendKeys(Keys.END).perform();
        SleepUtils.sleepSeconds(1);
        WebElement element = webDriver.findElement(By.cssSelector("a[ka=page-next]"));
        return element;
    }

    @Override
    public String parseUrl(String baseUrl, Document itemDocument) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        Element aElement = itemDocument.selectFirst("div[class=info-primary]").selectFirst("h3[class=name]").selectFirst("a");
        return baseUrl + aElement.attr("href");
    }
}
