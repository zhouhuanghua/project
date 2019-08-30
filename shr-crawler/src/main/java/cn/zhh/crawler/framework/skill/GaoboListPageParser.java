package cn.zhh.crawler.framework.skill;

import cn.zhh.crawler.framework.ListPageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 高博列表页解析器
 *
 * @author Zhou Huanghua
 */
@Component
public class GaoboListPageParser implements ListPageParser {
    @Override
    public void beforeProcess(WebDriver webDriver, Object parameter) {
        // nothing to do
    }

    @Override
    public List<Document> generateItems(Document document) {
        Elements elements = document.selectFirst("div[class=news_list3]").select("a[class=item]");
        return elements.stream().map(e -> Jsoup.parse(e.html())).collect(Collectors.toList());
    }

    @Override
    public WebElement nextPage(WebDriver webDriver) {
        // 暂无下一页
        return null;
    }
}
