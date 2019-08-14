package cn.zhh.crawler.service;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.Timezone;
import com.machinepublishers.jbrowserdriver.UserAgent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 浏览器驱动工厂类
 *
 * @author Zhou Huanghua
 */
@Component
public class WebDriverFactory {

    @Value("${selenium.webdriver.type:jbrowser}")
    private String driverType;

    @Value("${selenium.webdriver.chrome-driver-path:}")
    private String chromeDriverPath;

    public WebDriver openBrowser() {
        WebDriver webDriver;
        // Chrome驱动
        if (Objects.equals("chrome", driverType) && StringUtils.hasText(chromeDriverPath)) {
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            webDriver = new ChromeDriver();
        }
        // JBrowser驱动
        else {
            webDriver = new JBrowserDriver(Settings.builder()
                    .timezone(Timezone.ASIA_SHANGHAI)
                    .userAgent(UserAgent.CHROME).build());
        }
        webDriver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        webDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        return webDriver;
    }
}
