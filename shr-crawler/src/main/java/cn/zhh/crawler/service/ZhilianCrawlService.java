package cn.zhh.crawler.service;

import cn.zhh.common.constant.MqConsts;
import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.ErrorEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.util.BusinessException;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.crawler.util.Request;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 智联爬虫服务
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class ZhilianCrawlService implements CrawlService {

    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private ProxyService proxyService;
    private final String SEARCH_URL = "https://fe-api.zhaopin.com/c/i/sou";
    private final String PAGE_SIZE = "100";

    @Override
    public void crawl(SearchPositionInfoMsg searchCondition) throws Exception {
        // 执行搜索
        Map<String, String> paramsMap = buildParams(searchCondition);
        log.info("开始搜索职位，请求参数：{}", paramsMap);
        String rspStr = Request.builder().urlNonParams(SEARCH_URL).addQueryStringParameters(paramsMap)
            .addHeaders(proxyService.getCommonHeaderMap(SEARCH_URL))
            .build()
            .getByHttpClient();
        JSONObject response = JSONObject.parseObject(rspStr);
        if (!Objects.equals(response.getInteger("code"), 200)) {
            throw new BusinessException(ErrorEnum.BAD_REQUEST, "请求响应失败！");
        }

        // 获取总数
        JSONObject data = response.getJSONObject("data");
        Integer totalCount = data.getInteger("count");
        log.info("根据条件搜索到{}条职位数据！", totalCount);
        if (Objects.equals(totalCount, 0)) {
            return;
        }

        // 处理结果
        JSONArray results = data.getJSONArray("results");
        log.info("开始处理数据...");
        handleResults(results);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.SEARCH_POSITION_INFO_ZHILIAN_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MqConsts.SEARCH_POSITION_INFO_TOPIC_EXCHANGE_NAME, type = "topic"),
            key = ""
    ))
    @RabbitHandler
    @Override
    public void consumeMq(@Payload SearchPositionInfoMsg searchCondition, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        try {
            crawl(searchCondition);
        } catch (Exception e) {
            log.error("智联消费职位搜索消息异常！", e);
        }

        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }

    @Override
    @Async("asyncServiceExecutor")
    public void syncCrawl(SearchPositionInfoMsg searchCondition) {
        try {
            this.crawl(searchCondition);
        } catch (Exception e) {
            log.error("异步执行智联爬虫服务异常！", e);
        }
    }

    private Map<String, String> buildParams(SearchPositionInfoMsg searchCondition) {
        Map<String, String> conditionMap = new HashMap<>(16);

        // 1、搜索条件
        // 关键字
        conditionMap.put("kw", searchCondition.getContent());
        // 城市
        Byte city = searchCondition.getCity();
        conditionMap.put("cityId", SearchConditionConverter.getCity(city,
                SearchConditionConverter.SITE_NAME.ZHILIAN));

        // 2、常规条件
        conditionMap.put("pageSize", PAGE_SIZE);
        conditionMap.put("sortType", "publish");
        conditionMap.put("kt", "3");

        return conditionMap;
    }

    private void handleResults(JSONArray results) {
        AtomicInteger index = new AtomicInteger(1);
        results.stream()
            .map(value -> {
                if (value instanceof JSONObject) {
                    return (JSONObject)value;
                } else {
                    return value instanceof Map ? new JSONObject((Map)value) : (JSONObject) JSON.toJSON(value);
                }
            })
            .forEach(result -> {
                PositionInfoMsg positionInfoMsg = new PositionInfoMsg();
                positionInfoMsg.setUniqueKey(result.getString("number"));
                positionInfoMsg.setName(result.getString("jobName"));
                positionInfoMsg.setSource(PositionSourceEnum.ZHILIAN.getCode());
                positionInfoMsg.setSalary(result.getString("salary"));
                positionInfoMsg.setCity(result.getJSONObject("city").getString("display"));
                positionInfoMsg.setWorkExp(result.getJSONObject("workingExp").getString("name"));
                positionInfoMsg.setEducation(result.getJSONObject("eduLevel").getString("name"));
                positionInfoMsg.setWelfare(getWelfare(result.getJSONArray("welfare")));
                positionInfoMsg.setLabel(getPositionLabel(result.getJSONObject("positionLabel")));
                positionInfoMsg.setPublishTime(result.getDate("updateDate"));
                positionInfoMsg.setUrl(result.getString("positionURL"));
                JSONObject company = result.getJSONObject("company");
                positionInfoMsg.setCompanyName(company.getString("name"));
                positionInfoMsg.setCompanyLogo(result.getString("companyLogo"));
                positionInfoMsg.setCompanyScale(Optional.ofNullable(company.getJSONObject("size").getString("name")).orElse(""));

                // 分析详情页
                try {
                    analysisPositionDetail(positionInfoMsg.getUrl(), positionInfoMsg);
                } catch (Exception e) {
                    log.error("请求解析职位详情异常！", e);
                }

                // 推送MQ
                try {
                    log.info("推送第{}条职位数据到MQ，number={}", index.getAndIncrement(), positionInfoMsg.getUniqueKey());
                    mqProducer.sendPositionInfoMsg(positionInfoMsg);
                } catch (Exception e) {
                    log.error("推送职位信息消息到MQ异常！", e);
                }
            });
    }

    private String getWelfare(JSONArray jsonArray) {
        if (jsonArray.isEmpty()) {
            return "";
        }
        return jsonArray.toJavaList(String.class)
                .stream()
                .reduce((s1, s2) -> s1 + "," + s2).orElse("");
    }

    private String getPositionLabel(JSONObject jsonObject) {
        JSONArray skillLabelArray = jsonObject.getJSONArray("skillLabel");
        if (skillLabelArray.isEmpty()) {
            return "";
        }
        return skillLabelArray.toJavaList(JSONObject.class)
                .stream()
                .map(jsonObj -> jsonObj.getString("value"))
                .reduce((s1, s2) -> s1 + "," + s2).orElse("");
    }

    private PositionInfoMsg analysisPositionDetail(String positionDetailUrl, PositionInfoMsg positionInfoMsg) throws Exception {
        // 随机睡眠
        proxyService.defaultRandomSleep();

        // 访问详情页
        String htmlPage = Request.builder().urlNonParams(positionDetailUrl)
            .addHeaders(proxyService.getCommonHeaderMap(SEARCH_URL)).build().getByHttpClient();

        // 处理反爬
        htmlPage = handlePreventCrawl(htmlPage);

        Document document = Jsoup.parse(htmlPage);

    // start---------职位描述
        Element describtionElement = document.selectFirst("div[class=describtion]");
        StringBuilder describtion = new StringBuilder();
        describtion.append("职能要求：").append(SysConsts.LINE_SEPARATOR);
        // 技能要求
        OptionalOperationUtils.consumeIfNonNull(describtionElement.selectFirst("div[class=describtion__skills-content]"), describtionSkill -> {
            describtionSkill.children().forEach(element -> {
                describtion.append(element.text()).append(" ");
            });
        });
        // 岗位描述与职位要求
        describtion.append(SysConsts.LINE_SEPARATOR);
        OptionalOperationUtils.consumeIfNonNull(describtionElement.selectFirst("div[class=describtion__detail-content]"), describtionDetail -> {
            describtionDetail.children().forEach(element -> {
                if (element.children().isEmpty()) {
                    describtion.append(element.text()).append(SysConsts.LINE_SEPARATOR);
                } else {
                    element.children().forEach(e -> {
                        describtion.append(e.text()).append(SysConsts.LINE_SEPARATOR);
                    });
                }
            });
        });
        positionInfoMsg.setDescription(describtion.toString());
    // end---------职位描述

        // 工作地址
        OptionalOperationUtils.consumeIfNonNull(document.selectFirst("span[class=job-address__content-text]"), jobAddressElement -> {
            positionInfoMsg.setWorkAddress(jobAddressElement.text());
        });

        // 公司发展阶段（无）
        positionInfoMsg.setCompanyDevelopmentalStage("");

        // 经营领域
        OptionalOperationUtils.consumeIfNonNull(document.selectFirst("button[class='company__industry']"), companyDomainElement -> {
            positionInfoMsg.setCompanyDomain(companyDomainElement.text());
        });

        // 公司主页
        OptionalOperationUtils.consumeIfNonNull(document.selectFirst("a[class=company__page-site]"), companyUrlElement -> {
            positionInfoMsg.setCompanyUrl(companyUrlElement.attr("href"));
        });

        // 公司介绍
        OptionalOperationUtils.consumeIfNonNull(document.selectFirst("div[class=company__description]"), companyIntroductionElement -> {
            positionInfoMsg.setCompanyIntroduction(companyIntroductionElement.text());
        });

        return positionInfoMsg;
    }

    private String handlePreventCrawl(String htmlPage) {
        // 不包含指定内容的话，就是正常的网页
        if (!htmlPage.contains("为保证您的正常访问，请进行如下验证")) {
            return htmlPage;
        }

        Document document = Jsoup.parse(htmlPage);
        Element scriptElement = document.getElementsByTag("script").last();
        System.out.println(scriptElement.html());

        return htmlPage;
    }
}
