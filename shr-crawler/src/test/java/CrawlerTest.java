import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.CityEnum;
import cn.zhh.crawler.CrawlerApplication;
import cn.zhh.crawler.framework.CrawlTask;
import cn.zhh.crawler.framework.skill.GaoboDetailPageParser;
import cn.zhh.crawler.framework.skill.GaoboListPageParser;
import cn.zhh.crawler.service.PositionSearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 测试类
 *
 * @author Zhou Huanghua
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CrawlerApplication.class})
public class CrawlerTest {

    @Autowired
    private PositionSearchService positionSearchService;

    @Test
    public void test01() throws InterruptedException {
        positionSearchService.timeout();
        Thread.currentThread().join();
    }

    @Test
    public void test02() {
        String str = "更新于 01:00";
        Pattern pattern = Pattern.compile("[\\d]{2}:[\\d]{2}");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            String value = matcher.group();
            String[] hourMinute = matcher.group().split(":");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourMinute[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(hourMinute[1]));
            System.out.println(calendar.getTime());
        }
    }

    @Test
    public void test03() {
        String str = "更新于 8月20日";
        Pattern pattern = Pattern.compile("\\d+月\\d+日");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            String[] monthDay = matcher.group().replace("月", ":").replace("日", "").split(":");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, Integer.parseInt(monthDay[0]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(monthDay[1]));
            System.out.println(calendar.getTime());
            System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
    }

    @Test
    public void test04() {
        String str = "aaaa<br>bbb";
        String[] split = str.split("<br>");
    }

    @Test
    public void test05() throws InterruptedException {
        SearchPositionInfoMsg msg = new SearchPositionInfoMsg();
        msg.setContent("java");
        msg.setCity(CityEnum.BEIJING.getCode());
        positionSearchService.lagouSearch(msg);
        Thread.currentThread().join();
    }

    @Test
    public void test06() throws InterruptedException {
        SearchPositionInfoMsg msg = new SearchPositionInfoMsg();
        msg.setContent("java");
        msg.setCity(CityEnum.BEIJING.getCode());
        positionSearchService.zhilianSearch(msg);
        Thread.currentThread().join();
    }

    @Test
    public void test07() throws InterruptedException {
        SearchPositionInfoMsg msg = new SearchPositionInfoMsg();
        msg.setContent("java");
        msg.setCity(CityEnum.BEIJING.getCode());
        positionSearchService.bossSearch(msg);
        Thread.currentThread().join();
    }


    @Autowired
    private GaoboListPageParser gaoboListPageParser;
    @Autowired
    private GaoboDetailPageParser gaoboDetailPageParser;

    @Test
    public void testGaobo() throws IOException {
        CrawlTask.newInstance(null, "http://www.gem-inno.com/skills.html",
                gaoboListPageParser, 5, gaoboDetailPageParser, 1, 10)
                .start();
    }
}
