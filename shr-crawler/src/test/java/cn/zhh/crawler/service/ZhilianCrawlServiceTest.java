package cn.zhh.crawler.service;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.CityEnum;
import cn.zhh.crawler.framework.CrawlTask;
import cn.zhh.crawler.framework.zhilian.ZhilianDetailPageParser;
import cn.zhh.crawler.framework.zhilian.ZhilianListPageParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ZhilianCrawlServiceTest {

//    @Autowired
//    private ZhilianCrawlService zhilianCrawlService;

    @Autowired
    private WebDriverFactory webDriverFactory;

    @Autowired
    private ZhilianListPageParser zhilianListPageParser;

    @Autowired
    private ZhilianDetailPageParser zhilianDetailPageParser;

    /*@Test
    public void crawl() throws Exception {
        SearchPositionInfoMsg searchPositionInfoMsg = new SearchPositionInfoMsg();
        searchPositionInfoMsg.setContent("Python");
        searchPositionInfoMsg.setCity(CityEnum.GUANGZHOU.getCode());
        zhilianCrawlService.crawl(searchPositionInfoMsg);
        Thread.currentThread().join();
    }*/

    @Test
    public void crawl2() throws Exception {
        SearchPositionInfoMsg searchPositionInfoMsg = new SearchPositionInfoMsg();
        searchPositionInfoMsg.setContent("Java");
        searchPositionInfoMsg.setCity(CityEnum.SHANGHAI.getCode());
        CrawlTask.newInstance(searchPositionInfoMsg, webDriverFactory, "https://www.zhaopin.com/",
                zhilianListPageParser, 2, zhilianDetailPageParser, 0, 60).start();
    }

}