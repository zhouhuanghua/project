package cn.zhh.crawler.service.crawl;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.CityEnum;
import cn.zhh.crawler.util.Request;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 定时搜索服务
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class TimingSearchService {

    @Autowired
    private ApplicationContext applicationContext;

    private Set<Byte> cityList;

    private Set<String> positionList;

    private Set<CrawlService> crawlServiceSet;

    private void init() throws Exception {
        // 爬虫服务
        String[] beanNames = applicationContext.getBeanNamesForType(CrawlService.class);
        if (Objects.isNull(beanNames) || beanNames.length == 0) {
            return;
        }
        crawlServiceSet = Arrays.stream(beanNames)
                .map(n -> (CrawlService)applicationContext.getBean(n))
                .collect(Collectors.toSet());
        // 城市
        cityList = Arrays.stream(CityEnum.values())
            .filter(cityEnum -> !Objects.equals(cityEnum.getCode(), CityEnum.ALL.getCode()))
            .map(CityEnum::getCode)
            .collect(Collectors.toSet());
        // 职位
        String pageHtml = Request.builder().urlNonParams("https://www.lagou.com/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36")
                .build()
                .getByJsoup();
        Document document = Jsoup.parse(pageHtml);
        Elements aElements = document.getElementById("sidebar").getElementsByTag("a");
        positionList = aElements.stream()
            .map(a -> a.select("h3"))
            .flatMap(Elements::stream)
            .map(Element::text)
            .collect(Collectors.toSet());
    }

//    @Scheduled(cron = "* * 0/3 * * ?")
    public void timeout() {
        log.info("定时任务-搜索职位 开始运行...");
        try {
            init();
        } catch (Exception e) {
            log.error("定时任务-搜索职位 初始化数据异常！", e);
            return;
        }
        // 执行爬虫
        for (String position : positionList) {
            SearchPositionInfoMsg msg = new SearchPositionInfoMsg();
            msg.setContent(position);
            for (Byte city : cityList) {
                msg.setCity(city);
                for (CrawlService crawlService : crawlServiceSet) {
                    crawlService.syncCrawl(msg);
                }
            }
        }
    }
}
