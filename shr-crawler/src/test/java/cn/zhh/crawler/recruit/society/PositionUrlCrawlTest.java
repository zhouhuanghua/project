package cn.zhh.crawler.recruit.society;

import cn.zhh.crawler.CrawlerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CrawlerApplication.class})
public class PositionUrlCrawlTest {

    @Autowired
    private PositionUrlCrawlExecutor executor;

    @Test
    public void test() throws IOException, InterruptedException {
//        executor.crawlPositionUrlAndPushMq();
        Thread.currentThread().join();
    }
}