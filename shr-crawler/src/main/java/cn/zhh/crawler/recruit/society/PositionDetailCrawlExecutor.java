package cn.zhh.crawler.recruit.society;

import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.common.util.ThrowableUtils;
import cn.zhh.crawler.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/9 22:30
 */
@Slf4j
@Component
public class PositionDetailCrawlExecutor implements ApplicationContextAware {

    private final Map<String, String> COMMON_HEADER_MAP = MapUtils.buildMap(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36",
            "Accept", "application/json, text/plain, */*", "Cookie", "token");

    private Map<Byte, PositionDetailCrawlService> webSiteDetailServiceMapping;

    @Autowired
    private MqProducer mqProducer;

    public void consumeByJsoup(PositionUrlMsg positionUrlMsg) throws IOException {
        try {
            Connection connect = Jsoup.connect(positionUrlMsg.getDetailUrl());
            boolean success1 = parseDetailPage(connect.get(), positionUrlMsg);
            if (success1) {
                return;
            }
            SleepUtils.sleepSeconds(36);
            boolean sucess2 = parseDetailPage(connect.headers(COMMON_HEADER_MAP).get(), positionUrlMsg);
            if (sucess2) {
                return;
            }
        } catch (Throwable t) {
            log.error("Jsoup打开职位详情页面异常，positionUrlMsg：{}, e：{}",
                    positionUrlMsg, ThrowableUtils.getThrowableStackTrace(t));
        }
        // 推送补偿队列
        mqProducer.sendPositionUrlCompensateMsg(positionUrlMsg);
    }

    public void consumeBySelenium(PositionUrlMsg positionUrlMsg) throws IOException {
        WebDriver webDriver = BrowserDriverFactory.openJBrowser();
        try {
            for (int i = 1; i <= 2; ++i) {
                webDriver.get(positionUrlMsg.getDetailUrl());
                SleepUtils.sleepSeconds(2);
                boolean success = parseDetailPage(Jsoup.parse(webDriver.getPageSource()), positionUrlMsg);
                if (success) {
                    return;
                }
            }
        } catch (Throwable t) {
            log.error("Selenium打开职位详情页面异常，positionUrlMsg：{}, e：{}",
                    positionUrlMsg, ThrowableUtils.getThrowableStackTrace(t));
        } finally {
            OptionalOperationUtils.consumeIfNonNull(webDriver, WebDriver::quit);
        }
    }

    private boolean parseDetailPage(Document document, PositionUrlMsg positionUrlMsg) {
        PositionDetailCrawlService positionDetailCrawlService = webSiteDetailServiceMapping.get(positionUrlMsg.getWebsite());
        if (positionDetailCrawlService.isNormalPage(document)) {
            PositionInfoMsg positionInfoMsg = positionDetailCrawlService.generateObj(positionUrlMsg.getDetailUrl(), document);
            positionDetailCrawlService.convertWorkExp(positionInfoMsg);
            positionDetailCrawlService.convertEducation(positionInfoMsg);
            positionDetailCrawlService.convertCompanyDevelopmentalStage(positionInfoMsg);
            mqProducer.sendPositionInfoMsg(positionInfoMsg);
            return true;
        }
        return false;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanNamesForType(PositionDetailCrawlService.class);
        if (Objects.isNull(beanNames) || beanNames.length < 1) {
            webSiteDetailServiceMapping = Collections.emptyMap();
        }
        webSiteDetailServiceMapping = new HashMap<>(beanNames.length);
        Arrays.stream(beanNames)
                .map(n -> (PositionDetailCrawlService) applicationContext.getBean(n))
                .forEach(p -> {
                    webSiteDetailServiceMapping.put(p.webSite(), p);
                });
    }
}
