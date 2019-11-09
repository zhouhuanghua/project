package cn.zhh.crawler.recruit.society;

import cn.zhh.common.enums.CityEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.util.OptionalOperationUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
public class LagouPositionUrlCrawlService implements PositionUrlCrawlService {

    @Override
    public Byte webSite() {
        return PositionSourceEnum.LAGOU.getCode();
    }

    @Override
    public String baseUrl() {
        return "https://www.lagou.com";
    }

    @Override
    public void beforeProcess(WebDriver webDriver, Byte city, String position) {
        // 选择城市
        List<WebElement> aElements = webDriver.findElement(By.id("changeCityBox")).findElements(By.tagName("a"));
        Map<Byte, WebElement> cityMap = new HashMap<>(8);
        for (WebElement aElement : aElements) {
            String text = aElement.getText().trim();
            switch (text) {
                case "全国站":
                    cityMap.put(CityEnum.ALL.getCode(), aElement);
                    break;
                case "北京站":
                    cityMap.put(CityEnum.BEIJING.getCode(), aElement);
                    break;
                case "上海站":
                    cityMap.put(CityEnum.SHANGHAI.getCode(), aElement);
                    break;
                case "广州站":
                    cityMap.put(CityEnum.GUANGZHOU.getCode(), aElement);
                    break;
                case "深圳站":
                    cityMap.put(CityEnum.SHENZHEN.getCode(), aElement);
                    break;
                case "杭州站":
                    cityMap.put(CityEnum.HANGZHOU.getCode(), aElement);
                    break;
                case "成都站":
                    cityMap.put(CityEnum.CHENGDU.getCode(), aElement);
                    break;
                default:
                    break;
            }
        }
        if (Objects.isNull(city) || !cityMap.containsKey(city)) {
            cityMap.get(CityEnum.ALL.getCode()).click();
        }
        cityMap.get(city).click();

        SleepUtils.sleepSeconds(1);

        // 搜索
        webDriver.findElement(By.id("search_input")).sendKeys(position);
        webDriver.findElement(By.id("search_button")).click();

        SleepUtils.sleepSeconds(1);

        // 选择最新排序方式
        OptionalOperationUtils.consumeIfNonNull(webDriver.findElement(By.id("order")), orderElement -> {
            orderElement.findElement(By.cssSelector("li[class=wrapper]"))
                    .findElement(By.tagName("div")).findElements(By.tagName("a")).get(1).click();
        });
    }

    @Override
    public List<Document> generateItems(Document document) {
        Elements elements = document.getElementById("s_position_list").selectFirst("ul[class=item_con_list]").getElementsByTag("li");
        return elements.stream().map(e -> Jsoup.parse(e.html())).collect(Collectors.toList());
    }

    @Override
    public WebElement nextPage(WebDriver webDriver) {
        return webDriver.findElement(By.className("pager_next"));
    }

    @Override
    public String parseUrl(String baseUrl, Document itemDocument) {
        return itemDocument.selectFirst("a[class=position_link]").attr("href");
    }
}
