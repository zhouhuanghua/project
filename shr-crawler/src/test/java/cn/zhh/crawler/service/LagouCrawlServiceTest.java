package cn.zhh.crawler.service;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.CityEnum;
import cn.zhh.common.enums.DevelopmentStageEnum;
import cn.zhh.common.enums.EducationEnum;
import cn.zhh.common.enums.WorkExpEnum;
import cn.zhh.crawler.service.crawl.LagouCrawlService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class LagouCrawlServiceTest {

    @Autowired
    private LagouCrawlService lagouCrawlService;

    @Test
    public void crawl() throws Exception {
        SearchPositionInfoMsg searchPositionInfoMsg = new SearchPositionInfoMsg();
        searchPositionInfoMsg.setContent("Java");
        searchPositionInfoMsg.setCity(CityEnum.GUANGZHOU.getCode());
        searchPositionInfoMsg.setWorkExp(WorkExpEnum.ONE2THREE.getCode());
        searchPositionInfoMsg.setEducation(EducationEnum.UNDERGRADUATE.getCode());
        searchPositionInfoMsg.setDevelopmentStage(DevelopmentStageEnum.LISTED.getCode());
        lagouCrawlService.crawl(searchPositionInfoMsg);
        Thread.currentThread().join();
    }

}