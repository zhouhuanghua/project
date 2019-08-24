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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 智联列表页解析器
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class ZhilianListPageParser implements ListPageParser<SearchPositionInfoMsg> {

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
        sleep(1, TimeUnit.SECONDS);
        return webDriver.findElement(By.id("pagination_content"))
                .findElement(By.cssSelector("button[class='btn soupager__btn']"));
    }

    private void selectCity(WebDriver webDriver, Byte city) {
        // 切换到新窗口
        Iterator<String> iterator = webDriver.getWindowHandles().iterator();
        while (iterator.hasNext()) {
            String window = iterator.next();
            if (!Objects.equals(window, webDriver.getWindowHandle())) {
                webDriver.switchTo().window(window);
                break;
            }
        }
        // 点击弹出城市选择列表
        webDriver.findElement(By.id("queryTitleUls"))
                .findElement(By.cssSelector("li[class='currentCity query-city__uls__li current-city']"))
                .findElement(By.cssSelector("span[class='current-city__down span_down']")).click();
        // 构造元素与城市的映射关系
        sleep(500, TimeUnit.MILLISECONDS);
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
    }
}
