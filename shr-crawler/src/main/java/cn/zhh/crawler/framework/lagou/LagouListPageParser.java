package cn.zhh.crawler.framework.lagou;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.CityEnum;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.crawler.framework.ListPageParser;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 拉勾列表页解析器
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class LagouListPageParser implements ListPageParser<SearchPositionInfoMsg> {

    @Override
    public ListPageParser<SearchPositionInfoMsg> newInstance() {
        return new LagouListPageParser();
    }

    @Override
    public void beforeProcess(WebDriver webDriver, SearchPositionInfoMsg searchCondition) {
        // 选择城市
        selectCity(webDriver, searchCondition.getCity());
        sleep(1, TimeUnit.SECONDS);
        // 执行搜索
        webDriver.findElement(By.id("search_input")).sendKeys(searchCondition.getContent());
        webDriver.findElement(By.id("search_button")).click();
        sleep(2, TimeUnit.SECONDS);
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

    private void selectCity(WebDriver webDriver, Byte city) {
        // 遍历选择城市里面的全部a标签，构成映射关系
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
        // 如果为空，或者不在映射集合里面，则点击全国站
        if (Objects.isNull(city) || !cityMap.containsKey(city)) {
            cityMap.get(CityEnum.ALL.getCode()).click();
        }
        // 按值点击对应城市
        cityMap.get(city).click();
    }
}
