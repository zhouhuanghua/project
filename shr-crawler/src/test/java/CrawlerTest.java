import cn.zhh.crawler.CrawlerApplication;
import cn.zhh.crawler.service.PositionSearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
}
