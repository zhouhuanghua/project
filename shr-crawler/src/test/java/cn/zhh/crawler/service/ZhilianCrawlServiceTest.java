package cn.zhh.crawler.service;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
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
        searchPositionInfoMsg.setContent("大数据");
        zhilianCrawlService.crawl(searchPositionInfoMsg);
        Thread.currentThread().join();
    }

}