package cn.zhh.crawler.service;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.CityEnum;
import cn.zhh.crawler.framework.CrawlTask;
import cn.zhh.crawler.framework.boss.BossDetailPageParser;
import cn.zhh.crawler.framework.boss.BossListPageParser;
import cn.zhh.crawler.framework.lagou.LagouDetailPageParser;
import cn.zhh.crawler.framework.lagou.LagouListPageParser;
import cn.zhh.crawler.framework.zhilian.ZhilianDetailPageParser;
import cn.zhh.crawler.framework.zhilian.ZhilianListPageParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 职位搜索服务
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class PositionSearchService {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);

    private final String POSITION_FILE_PATH = "classpath:static/it_position.txt";

    @Autowired
    private BossListPageParser bossListPageParser;
    @Autowired
    private BossDetailPageParser bossDetailPageParser;
    @Autowired
    private LagouListPageParser lagouListPageParser;
    @Autowired
    private LagouDetailPageParser lagouDetailPageParser;
    @Autowired
    private ZhilianListPageParser zhilianListPageParser;
    @Autowired
    private ZhilianDetailPageParser zhilianDetailPageParser;

    private Set<Byte> citySet;

    private Set<String> positionSet;

    public PositionSearchService() {
        try {
            init();
        } catch (Exception e) {
            log.error("搜索职位服务初始化城市列表和职位列表异常！", e);
        }
    }

    private void init() throws Exception {
        // 城市（从枚举类获取）
        citySet = Arrays.stream(CityEnum.values())
                .filter(cityEnum -> !Objects.equals(cityEnum.getCode(), CityEnum.ALL.getCode()))
                .map(CityEnum::getCode)
                .collect(Collectors.toSet());
        // 职位（从配置文件读取）
        File file = ResourceUtils.getFile(POSITION_FILE_PATH);
        List<String> positionList = Files.readAllLines(file.toPath());
        positionSet = new HashSet<>(positionList);
    }

    @Scheduled(cron = "* * 0/6 * * ?")
    public void timeout() {
        log.info("定时任务-搜索职位 开始运行...");
        // 执行爬虫
        for (String position : positionSet) {
            SearchPositionInfoMsg msg = new SearchPositionInfoMsg();
            msg.setContent(position);
            for (Byte city : citySet) {
                msg.setCity(city);
                log.info("定时任务开始爬取，职位：{}，城市：{}", msg.getContent(), msg.getCity());
                EXECUTOR_SERVICE.execute(() -> bossSearch(msg));
                EXECUTOR_SERVICE.execute(() -> lagouSearch(msg));
                EXECUTOR_SERVICE.execute(() -> zhilianSearch(msg));
            }
        }
    }

    public void bossSearch(SearchPositionInfoMsg searchPositionInfoMsg) {
        try {
            CrawlTask.newInstance(searchPositionInfoMsg, "https://www.zhipin.com/", bossListPageParser,
                    5, bossDetailPageParser, 1, 30).start();
        } catch (Exception e) {
            log.error("boss搜索职位异常！", e);
        }
    }

    public void lagouSearch(SearchPositionInfoMsg searchPositionInfoMsg) {
        try {
            CrawlTask.newInstance(searchPositionInfoMsg, "https://www.lagou.com/", lagouListPageParser,
                    5, lagouDetailPageParser, 1, 36).start();
        } catch (IOException e) {
            log.error("lagou搜索职位异常！", e);

        }
    }

    public void zhilianSearch(SearchPositionInfoMsg searchPositionInfoMsg) {
        try {
            CrawlTask.newInstance(searchPositionInfoMsg, "https://www.zhaopin.com/", zhilianListPageParser,
                    3, zhilianDetailPageParser, 1, 10).start();
        } catch (IOException e) {
            log.error("zhilian搜索职位异常！", e);
        }
    }
}
