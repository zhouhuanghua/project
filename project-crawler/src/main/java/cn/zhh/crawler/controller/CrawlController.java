package cn.zhh.crawler.controller;

import cn.zhh.common.constant.CityEnum;
import cn.zhh.crawler.runner.CrawlUrlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 控制器
 *
 * @author Zhou Huanghua
 */
@RestController
public class CrawlController {

    @Autowired
    private CrawlUrlRunner crawlUrlRunner;


    @GetMapping("crawl/url")
    public String crawlUrl(@RequestParam String city, @RequestParam String jobName, @RequestParam Integer pageNum) {
        Optional<CityEnum> cityEnumOptional = CityEnum.getByDesc(city);
        if (cityEnumOptional.isPresent()) {
            crawlUrlRunner.crawlUrl(cityEnumOptional.get(), jobName, pageNum);
            return "OK";
        }
        String citiesStr = Arrays.stream(CityEnum.values()).map(CityEnum::getDesc).collect(Collectors.joining("、"));
        return "参数错误，可供选择的城市列表：" + citiesStr;
    }
}
