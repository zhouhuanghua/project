package cn.zhh.crawler.framework;

import cn.zhh.crawler.constant.CrawlerConsts;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.Timezone;
import com.machinepublishers.jbrowserdriver.UserAgent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;
import java.util.Objects;

/**
 * 浏览器驱动工厂类
 *
 * @author Zhou Huanghua
 */
public class BrowserDriverFactory {

    private static final String CHROME_DRIVER_PATH = "src/main/resources/static/chromedriver.exe";

    private BrowserDriverFactory() {
        throw new UnsupportedOperationException("不支持创建实例！");
    }

    public static WebDriver openBrowser(DriverType driverType) {
        WebDriver webDriver;
        // chrome
        if (Objects.equals(DriverType.CHROME, driverType)) {
            System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
            webDriver = new ChromeDriver(chromeOptions);
        }
        // jbrowser
        else if (Objects.equals(DriverType.JBROWSER, driverType)) {
            webDriver = new JBrowserDriver(Settings.builder()
                    .timezone(Timezone.ASIA_SHANGHAI)
                    .userAgent(UserAgent.CHROME).build());
        }
        // unknow
        else {
            throw new RuntimeException("未知的驱动类型！");
        }

        webDriver.manage().timeouts().pageLoadTimeout(CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_VALUE, CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_UNIT);
        webDriver.manage().timeouts().setScriptTimeout(CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_VALUE, CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_UNIT);
        webDriver.manage().timeouts().implicitlyWait(CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_VALUE, CrawlerConsts.BROWSER_OPENPAGE_TIMEOUT_UNIT);
        return webDriver;
    }

    public static enum DriverType {
        CHROME, JBROWSER;
    }
}
