package cn.zhh.crawler.runner;

import cn.zhh.crawler.constant.CrawlerConsts;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.Timezone;
import com.machinepublishers.jbrowserdriver.UserAgent;
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
        String classesPath = BrowserDriverFactory.class.getClassLoader().getResource("").getPath();
        System.setProperty("webdriver.chrome.driver", classesPath.substring(1) + "static/chromedriver80.exe");
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

    public static WebDriver openJBrowser() {
        WebDriver webDriver = new JBrowserDriver(Settings.builder()
                .timezone(Timezone.ASIA_SHANGHAI)
                .userAgent(UserAgent.CHROME).build());
        initializeSetup(webDriver);
        return webDriver;
    }

    private static void initializeSetup(WebDriver webDriver) {
        webDriver.manage().timeouts().pageLoadTimeout(CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_VALUE, CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_UNIT);
        webDriver.manage().timeouts().setScriptTimeout(CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_VALUE, CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_UNIT);
        webDriver.manage().timeouts().implicitlyWait(CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_VALUE, CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_UNIT);
    }
}
