package cn.zhh.crawler;

import cn.zhh.common.constant.CityEnum;
import cn.zhh.common.constant.Consts;
import cn.zhh.crawler.runner.CrawlUrlRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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

    @Test
    public void testParseDesc() throws IOException {
        File file = ResourceUtils.getFile("classpath:descText");
        String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        text = text
            // <p>段落替换为换行
            .replaceAll("<p.*?>", Consts.LINE_SEPARATOR)
            // <br><br/>替换为换行
            .replaceAll("<br\\s*/?>", Consts.LINE_SEPARATOR)
            // 去掉其它的<>之间的东西
            .replaceAll("\\<.*?>", "")
            // 去掉&nbsp;
            .replaceAll("&nbsp;", "");
        for (String s : text.split(Consts.LINE_SEPARATOR)) {
            if (StringUtils.hasText(s)) {
                System.out.println(s);
            }
        }
    }
}
