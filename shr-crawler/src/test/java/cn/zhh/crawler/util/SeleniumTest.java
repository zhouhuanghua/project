package cn.zhh.crawler.util;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
public class SeleniumTest {

    @Test
    public void test() {
        WebDriver webDriver = new HtmlUnitDriver();
        webDriver.get("https://www.lagou.com/");
        webDriver.findElement(By.id("search_input")).sendKeys("Java");
        webDriver.findElement(By.id("search_button")).click();
        System.out.println(webDriver.getPageSource());
    }
}
