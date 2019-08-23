package cn.zhh.crawler.framework.zhilian;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.CityEnum;
import cn.zhh.crawler.framework.ListPageParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class ZhilianListPageParser implements ListPageParser<SearchPositionInfoMsg> {

    @Override
    public ListPageParser<SearchPositionInfoMsg> newInstance() {
        return new ZhilianListPageParser();
    }

    @Override
    public void beforeProcess(WebDriver webDriver, SearchPositionInfoMsg parameter) {
        // 关闭弹窗
        webDriver.findElement(By.cssSelector("div[class=risk-warning__content]"))
                .findElement(By.tagName("button")).click();
        // 搜索
        webDriver.findElement(By.cssSelector("input[class=zp-search__input]")).sendKeys(parameter.getContent());
        webDriver.findElement(By.cssSelector("a[class='zp-search__btn zp-search__btn--blue']")).click();
        // 选择城市
        selectCity(webDriver, parameter.getCity());
    }

    @Override
    public List<Document> generateItems(Document document) {
        Elements elements = document.selectFirst("div[class=job-list]").selectFirst("ul").getElementsByTag("li");
        return elements.stream().map(e -> Jsoup.parse(e.html())).collect(Collectors.toList());
    }

    @Override
    public WebElement nextPage(WebDriver webDriver) {
        WebElement element = webDriver.findElement(By.cssSelector("a[ka=page-next]"));
        new Actions(webDriver).sendKeys(Keys.END).perform();
        sleep(1, TimeUnit.SECONDS);
        return element;
    }

    private void selectCity(WebDriver webDriver, Byte city) {
        // 点击"选择城市"
        webDriver.findElement(By.cssSelector("span[class=switchover-city]")).click();
        // 鼠标放在热门选项上
        List<WebElement> liElements = webDriver.findElement(By.cssSelector("ul[class=dorpdown-province]")).findElements(By.tagName("li"));
        Actions actions = new Actions(webDriver);
        actions.moveToElement(liElements.get(0)).perform();
        // 选择对应城市
        List<WebElement> liElementList = webDriver.findElement(By.cssSelector("div[class=dorpdown-city]"))
                .findElement(By.cssSelector("ul[class=show]")).findElements(By.tagName("li"));
        Map<Byte, WebElement> cityMap = new HashMap<>(8);
        for (WebElement liElement : liElementList) {
            String text = liElement.getAttribute("ka");
            switch (text) {
                case "hot-city-100010000":
                    cityMap.put(CityEnum.ALL.getCode(), liElement);
                    break;
                case "101010100":
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
    }
}
