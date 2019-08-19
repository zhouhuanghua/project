package cn.zhh.crawler.service;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.CityEnum;
import cn.zhh.crawler.framework.CrawlTask;
import cn.zhh.crawler.framework.lagou.LagouDetailPageParser;
import cn.zhh.crawler.framework.lagou.LagouListPageParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class LagouCrawlServiceTest {

    /*@Autowired
    private LagouCrawlService lagouCrawlService;*/

    @Autowired
    private WebDriverFactory webDriverFactory;

    @Autowired
    private LagouListPageParser lagouListPageParser;

    @Autowired
    private LagouDetailPageParser lagouDetailPageParser;

    /*@Test
    public void crawl() throws Exception {
        SearchPositionInfoMsg searchPositionInfoMsg = new SearchPositionInfoMsg();
        searchPositionInfoMsg.setContent("人工智能");
        searchPositionInfoMsg.setCity(CityEnum.GUANGZHOU.getCode());
        lagouCrawlService.crawl(searchPositionInfoMsg);
        Thread.currentThread().join();
    }*/

    @Test
    public void crawl2() throws Exception {
        SearchPositionInfoMsg searchPositionInfoMsg = new SearchPositionInfoMsg();
        searchPositionInfoMsg.setContent("python");
        searchPositionInfoMsg.setCity(CityEnum.CHENGDU.getCode());
        CrawlTask.newInstance(searchPositionInfoMsg, webDriverFactory, "https://www.lagou.com/",
                lagouListPageParser, 1, lagouDetailPageParser, 0, 0).start();
    }

}