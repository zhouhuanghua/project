package cn.zhh.crawler.service;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.CityEnum;
import cn.zhh.crawler.service.crawl.ZhilianCrawlService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ZhilianCrawlServiceTest {

    @Autowired
    private ZhilianCrawlService zhilianCrawlService;

    @Test
    public void crawl() throws Exception {
        SearchPositionInfoMsg searchPositionInfoMsg = new SearchPositionInfoMsg();
        searchPositionInfoMsg.setContent("Python");
        searchPositionInfoMsg.setCity(CityEnum.GUANGZHOU.getCode());
        zhilianCrawlService.crawl(searchPositionInfoMsg);
        Thread.currentThread().join();
    }

}