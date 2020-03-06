package cn.zhh.crawler;

import cn.zhh.common.constant.CityEnum;
import cn.zhh.crawler.runner.CrawlUrlRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 测试
 *
 * @author Zhou Huanghua
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CrawlerApplication.class})
public class MyTest {

    @Autowired
    private CrawlUrlRunner crawlUrlRunner;

    @Test
    public void testCrawlUrl() {
        crawlUrlRunner.crawlUrl(CityEnum.GUANGZHOU, "Java", 5);
    }
}
