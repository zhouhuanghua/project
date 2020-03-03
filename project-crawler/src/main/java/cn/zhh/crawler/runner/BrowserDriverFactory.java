package cn.zhh.crawler.runner;

import cn.zhh.crawler.constant.Consts;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;

/**
 * 浏览器驱动工厂类
 *
 * @author Zhou Huanghua
 */
public class BrowserDriverFactory {

    private static final ChromeOptions CHROME_OPTIONS = new ChromeOptions();

    static {
        System.setProperty("webdriver.chrome.driver", "D:\\IdeaProjects\\project\\project-crawler\\src\\main\\resources\\static\\chromedriver80.exe");
        CHROME_OPTIONS.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
    }
    private BrowserDriverFactory() {
        throw new UnsupportedOperationException("不支持创建实例！");
    }

    public static WebDriver openChromeBrowser() {
        WebDriver webDriver = new ChromeDriver(CHROME_OPTIONS);
        initializeSetup(webDriver);
        return webDriver;
    }

    private static void initializeSetup(WebDriver webDriver) {
        webDriver.manage().timeouts().pageLoadTimeout(Consts.BROWSER_OPENPAGE_TIMEOUT_VALUE, Consts.BROWSER_OPENPAGE_TIMEOUT_UNIT);
        webDriver.manage().timeouts().setScriptTimeout(Consts.BROWSER_OPENPAGE_TIMEOUT_VALUE, Consts.BROWSER_OPENPAGE_TIMEOUT_UNIT);
        webDriver.manage().timeouts().implicitlyWait(Consts.BROWSER_OPENPAGE_TIMEOUT_VALUE, Consts.BROWSER_OPENPAGE_TIMEOUT_UNIT);
    }
}
