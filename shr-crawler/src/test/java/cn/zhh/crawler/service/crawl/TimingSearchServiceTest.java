package cn.zhh.crawler.service.crawl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TimingSearchServiceTest {

    @Autowired
    private TimingSearchService searchService;

    @Test
    public void timeout() throws Exception {
        searchService.timeout();
        Thread.currentThread().join();
    }

}