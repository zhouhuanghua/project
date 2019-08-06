package cn.zhh.crawler.service;

import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.ErrorEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.util.BusinessException;
import cn.zhh.crawler.constant.CrawlerConsts;
import cn.zhh.crawler.util.FunctionUtils;
import cn.zhh.crawler.util.HttpClientUtils;
import cn.zhh.crawler.util.ProxyUtils;
import cn.zhh.crawler.util.ZhilianSearchConditionConvertUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void crawl(SearchPositionInfoMsg searchCondition) throws Exception {
        // 执行搜索
        Map<String, String> paramsMap = buildParams(searchCondition);
        log.info("开始搜索职位，请求参数：{}", paramsMap);
        String rspStr = HttpClientUtils.get(CrawlerConsts.ZHILIAN_SEARCH_URL, paramsMap, CrawlerConsts.HEADER_MAP);
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

    private Map<String, String> buildParams(SearchPositionInfoMsg searchCondition) {
        Map<String, String> conditionMap = new HashMap<>(16);

        // 1、搜索条件
        // 关键字
        conditionMap.put("kw", searchCondition.getContent());
        // 城市
        FunctionUtils.runIfNotBlank(searchCondition.getCity(), () ->
                conditionMap.put("cityId", ZhilianSearchConditionConvertUtils.getCity(searchCondition.getCity()))
        );
        // 工作经验
        FunctionUtils.runIfNotBlank(searchCondition.getWorkExp(), () ->
                conditionMap.put("workExperience", ZhilianSearchConditionConvertUtils.getworkExp(searchCondition.getWorkExp()))
        );
        // 学历
        FunctionUtils.runIfNotBlank(searchCondition.getEducation(), () ->
                conditionMap.put("education", ZhilianSearchConditionConvertUtils.getEducation(searchCondition.getEducation()))
        );

        // 2、常规条件
        conditionMap.put("pageSize", CrawlerConsts.PAGE_SIZE);
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
        ProxyUtils.randomSleep();

        // 访问详情页
        String htmlPage = HttpClientUtils.get(positionDetailUrl, null, CrawlerConsts.HEADER_MAP);

        // 处理反爬
        htmlPage = handlePreventCrawl(htmlPage);

        Document document = Jsoup.parse(htmlPage);

    // start---------职位描述
        Element describtionElement = document.selectFirst("div[class=describtion]");
        StringBuilder describtion = new StringBuilder();
        describtion.append("职能要求：").append(SysConsts.LINE_SEPARATOR);
        // 技能要求
        Element describtionSkill = describtionElement.selectFirst("div[class=describtion__skills-content]");
        FunctionUtils.runIfNonNull(describtionSkill, () -> {
            describtionSkill.children().forEach(element -> {
                describtion.append(element.text()).append(" ");
            });
        });
        // 岗位描述与职位要求
        describtion.append(SysConsts.LINE_SEPARATOR);
        Element describtion_Detail = describtionElement.selectFirst("div[class=describtion__detail-content]");
        FunctionUtils.runIfNonNull(describtion_Detail, () -> {
            describtion_Detail.children().forEach(element -> {
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
        Element jobAddressElement = document.selectFirst("span[class=job-address__content-text]");
        FunctionUtils.runIfNonNull(jobAddressElement, () -> {
            positionInfoMsg.setWorkAddress(jobAddressElement.text());
        });

        // 公司发展阶段（无）
        positionInfoMsg.setCompanyDevelopmentalStage("");

        // 经营领域
        Element companyDomainElement = document.selectFirst("button[class='company__industry']");
        FunctionUtils.runIfNonNull(companyDomainElement, () -> {
            positionInfoMsg.setCompanyDomain(companyDomainElement.text());
        });

        // 公司主页
        Element companyUrlElement = document.selectFirst("a[class=company__page-site]");
        FunctionUtils.runIfNonNull(companyUrlElement, () -> {
            positionInfoMsg.setCompanyUrl(companyUrlElement.attr("href"));
        });

        // 公司介绍
        Element companyIntroductionElement = document.selectFirst("div[class=company__description]");
        FunctionUtils.runIfNonNull(companyIntroductionElement, () -> {
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


        Context context = Context.enter();
        ScriptableObject scriptableObject = context.initStandardObjects();
        context.evaluateString(scriptableObject, "", "", 1, null);
        Object var = scriptableObject.get("", scriptableObject);

        return htmlPage;
    }
}
