package cn.zhh.crawler.service;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.CityEnum;
import cn.zhh.crawler.framework.CrawlTask;
import cn.zhh.crawler.framework.boss.BossDetailPageParser;
import cn.zhh.crawler.framework.boss.BossListPageParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class BossCrawlServiceTest {

    /*@Autowired
    private BossCrawlService bossCrawlService;*/

    @Autowired
    private WebDriverFactory webDriverFactory;

    @Autowired
    private BossListPageParser bossListPageParser;

    @Autowired
    private BossDetailPageParser bossDetailPageParser;

    /*@Test
    public void crawl() throws Exception {
        SearchPositionInfoMsg searchPositionInfoMsg = new SearchPositionInfoMsg();
        searchPositionInfoMsg.setContent("Java");
        searchPositionInfoMsg.setCity(CityEnum.CHENGDU.getCode());
        bossCrawlService.crawl(searchPositionInfoMsg);
    }*/

    @Test
    public void crawl2() throws Exception {
        SearchPositionInfoMsg searchPositionInfoMsg = new SearchPositionInfoMsg();
        searchPositionInfoMsg.setContent("Java");
        searchPositionInfoMsg.setCity(CityEnum.SHANGHAI.getCode());
        CrawlTask.newInstance(searchPositionInfoMsg, webDriverFactory, "https://www.zhipin.com/",
                bossListPageParser, 2, bossDetailPageParser, 0, 60).start();
    }

}