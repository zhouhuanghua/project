package cn.zhh.crawler.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
public class SeleniumTest {

    @Test
    public void test() throws IOException {
        /*int i = 5;
        System.out.println(++i < 6);*/
        String str = "经验1-3年 /";
        System.out.println(str.replaceAll("\\s*/\\s*", ""));

    }

    @Test
    public void test2() {
        // 设置驱动位置(真实浏览器)
        System.setProperty("webdriver.chrome.driver", "src/main/resources/static/chromedriver.exe");

        // 创建一个驱动对象
        WebDriver driver = new ChromeDriver();

        // 窗口最大化
        driver.manage().window().maximize();

        // 打开指定网页
        String url = "https://www.lagou.com/";
        driver.navigate().to(url);

        // 智能等待一下子
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        driver.findElement(By.id("changeCityBox")).findElement(By.tagName("ul")).findElements(By.tagName("li")).get(0).click();

        driver.findElement(By.id("search_input")).sendKeys("Java");
        driver.findElement(By.id("search_button")).click();


        WebElement positionListWebElement = driver.findElement(By.id("s_position_list"));

        positionListWebElement.findElement(By.cssSelector("ul[class=item_con_list]"))
            .findElements(By.tagName("li"))
            .forEach(positionWebElement -> {
                String href = positionListWebElement.findElement(By.cssSelector("a[class=position_link]")).getAttribute("href");
                try {
                    Document document = Jsoup.connect(href).get();
                    System.out.println(document);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            });

    }
}
