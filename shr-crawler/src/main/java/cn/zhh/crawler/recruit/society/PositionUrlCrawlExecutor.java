package cn.zhh.crawler.recruit.society;

import cn.zhh.common.enums.CityEnum;
import cn.zhh.common.enums.ErrorEnum;
import cn.zhh.common.util.BusinessException;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.common.util.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/9 15:32
 */
@Slf4j
@Component
public class PositionUrlCrawlExecutor implements ApplicationContextAware {

    private static final String POSITION_FILE_PATH = "classpath:static/it_position.txt";

    private static final int PAGE_MAX = 10;

    private List<PositionUrlCrawlService> positionUrlCrawlServiceList;

    @Autowired
    private Executor asyncExecutor;

    @Autowired
    private MqProducer mqProducer;

    public void crawlPositionUrlAndPushMq() throws IOException {
        // 城市（从枚举类获取）
        Set<Byte> citySet = Arrays.stream(CityEnum.values())
                .filter(cityEnum -> !Objects.equals(cityEnum.getCode(), CityEnum.ALL.getCode()))
                .map(CityEnum::getCode)
                .collect(Collectors.toSet());
        // 职位（从配置文件读取）
        File file = ResourceUtils.getFile(POSITION_FILE_PATH);
        List<String> positionList = Files.readAllLines(file.toPath());
        Set<String> positionSet = positionList.stream().filter(p -> p.startsWith("## "))
                .map(p -> p.substring(3))
                .collect(Collectors.toSet());
        // 映射
        Set<Pair<Byte, String>> mappingSet = new HashSet<>();
        for (Byte c : citySet) {
            for (String p : positionSet) {
                mappingSet.add(Pair.of(c, p));
            }
        }

        if (mappingSet.isEmpty()) {
            throw new BusinessException(ErrorEnum.RECORD_IS_EMPTY, "没有需要搜索的职位！");
        }
        if (positionUrlCrawlServiceList.isEmpty()) {
            throw new BusinessException(ErrorEnum.RECORD_IS_EMPTY, "没有可用的PositionUrlCrawlService！");
        }
        for (PositionUrlCrawlService positionUrlCrawlService : positionUrlCrawlServiceList) {
            asyncExecutor.execute(() -> run(positionUrlCrawlService, mappingSet));
        }
    }

    private void run(PositionUrlCrawlService positionUrlCrawlService, Set<Pair<Byte, String>> mappingSet) {
        Deque<Pair<Byte, String>> pairDeque = new ArrayDeque<>(mappingSet);
        CompletableFuture.allOf(CompletableFuture.runAsync(() -> {
            Pair<Byte, String> mapping;
            while (Objects.nonNull(mapping = pairDeque.pollFirst())) {
                doCrawl(positionUrlCrawlService, mapping);
            }
        }, asyncExecutor), CompletableFuture.runAsync(() -> {
            Pair<Byte, String> mapping;
            while (Objects.nonNull(mapping = pairDeque.pollLast())) {
                doCrawl(positionUrlCrawlService, mapping);
            }
        }, asyncExecutor)).join();
    }

    private void doCrawl(PositionUrlCrawlService positionUrlCrawlService, Pair<Byte, String> mapping) {
        WebDriver webDriver = BrowserDriverFactory.openChromeBrowser();
        try {
            webDriver.manage().window().maximize();
            SleepUtils.sleepSeconds(1);
            webDriver.get(positionUrlCrawlService.baseUrl());
            SleepUtils.sleepSeconds(2);
            positionUrlCrawlService.beforeProcess(webDriver, mapping.getFirst(), mapping.getSecond());
            for (int pageNum = 1; pageNum <= PAGE_MAX; ++pageNum) {
                SleepUtils.sleepSeconds(3);
                List<Document> itemDocumentList = positionUrlCrawlService.generateItems(Jsoup.parse(webDriver.getPageSource()));
                for (Document itemDocument : itemDocumentList) {
                    String url = positionUrlCrawlService.parseUrl(positionUrlCrawlService.baseUrl(), itemDocument);
                    mqProducer.sendPositionUrlMsg(new PositionUrlMsg(positionUrlCrawlService.webSite(), url));
                }
                WebElement nextPageElement = positionUrlCrawlService.nextPage(webDriver);
                if (Objects.isNull(nextPageElement)) {
                    break;
                }
                nextPageElement.click();
            }
        } catch (Throwable t) {
            log.error("爬取职位链接异常，positionUrlCrawlService：{}，mapping：{}, e：{}",
                    positionUrlCrawlService, mapping, ThrowableUtils.getThrowableStackTrace(t));
        } finally {
            OptionalOperationUtils.consumeIfNonNull(webDriver, WebDriver::quit);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanNamesForType(PositionUrlCrawlService.class);
        if (Objects.isNull(beanNames) || beanNames.length < 1) {
            positionUrlCrawlServiceList = Collections.emptyList();
        }
        positionUrlCrawlServiceList = Arrays.stream(beanNames)
                .map(n -> (PositionUrlCrawlService) applicationContext.getBean(n))
                .collect(Collectors.toList());
    }
}
