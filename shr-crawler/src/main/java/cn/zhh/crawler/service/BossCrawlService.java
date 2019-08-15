package cn.zhh.crawler.service;

import cn.zhh.common.constant.MqConsts;
import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.DevelopmentStageEnum;
import cn.zhh.common.enums.EducationEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.enums.WorkExpEnum;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.crawler.util.Request;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Boss直聘爬虫服务
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class BossCrawlService implements CrawlService {

    private final String BASE_URL = "https://www.zhipin.com/";
    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private ProxyService proxyService;

    @Override
    public void crawl(SearchPositionInfoMsg searchCondition) throws Exception {
        // 按搜索条件构造请求建造器
        Request.Builder queryBuilder = Request.builder().urlNonParams(BASE_URL + "/job_detail")
                .addHeaders(proxyService.getCommonHeaderMap(BASE_URL));
        handleSearchCondition(queryBuilder, searchCondition);
        int pageNum = 1;

        // 处理第一页
        Document document = Jsoup.parse(queryBuilder.proxy(proxyService.getRandomProxyAddress()).build().getByJsoup());
        handleEveryPage(document, pageNum);

        // 处理下一页（暂时爬取10页）
        for (; Objects.nonNull(document.selectFirst("div[class=page]").selectFirst("a[class=next]"))
                && ++pageNum < 11; ) {
            proxyService.sleep(5, TimeUnit.SECONDS);
            document = Jsoup.parse(queryBuilder.proxy(proxyService.getRandomProxyAddress())
                    .addQueryStringParameter("page", String.valueOf(pageNum)).build().getByJsoup());
            handleEveryPage(document, pageNum);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.SEARCH_POSITION_INFO_BOSS_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MqConsts.SEARCH_POSITION_INFO_TOPIC_EXCHANGE_NAME, type = "topic"),
            key = ""
    ))
    @RabbitHandler
    @Override
    public void consumeMq(@Payload SearchPositionInfoMsg searchCondition, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        try {
            crawl(searchCondition);
        } catch (Exception e) {
            log.error("Boss消费职位搜索消息异常！", e);
        }

        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }

    @Override
    @Async("asyncServiceExecutor")
    public void syncCrawl(SearchPositionInfoMsg searchCondition) {
        try {
            log.info("boss爬虫服务异步启动，职位：{}，城市：{}", searchCondition.getContent(), searchCondition.getCity());
            this.crawl(searchCondition);
        } catch (Exception e) {
            log.error("异步执行boss爬虫服务异常！", e);
        }
    }

    @Override
    public void convert(PositionInfoMsg positionInfoMsg) {
        // 工作经验转换
        OptionalOperationUtils.consumeIfNotBlank(positionInfoMsg.getWorkExp(), workExp -> {
            switch (workExp) {
                case "经验不限":
                    positionInfoMsg.setWorkExp(WorkExpEnum.NOT_REQUIRED.getDescription());
                    break;
                case "应届生":
                    positionInfoMsg.setWorkExp(WorkExpEnum.NONE.getDescription());
                    break;
                case "1年以内":
                    positionInfoMsg.setWorkExp(WorkExpEnum.LESS1.getDescription());
                    break;
                case "1-3年":
                    positionInfoMsg.setWorkExp(WorkExpEnum.ONE2THREE.getDescription());
                    break;
                case "3-5年":
                    positionInfoMsg.setWorkExp(WorkExpEnum.THREE2FIVE.getDescription());
                    break;
                case "5-10年":
                    positionInfoMsg.setWorkExp(WorkExpEnum.FIVE2TEN.getDescription());
                    break;
                case "10年以上":
                    positionInfoMsg.setWorkExp(WorkExpEnum.MORE10.getDescription());
                    break;
                default:
                    positionInfoMsg.setWorkExp(WorkExpEnum.NOT_REQUIRED.getDescription());
                    break;
            }
        });
        // 学历转换
        OptionalOperationUtils.consumeIfNotBlank(positionInfoMsg.getWorkExp(), workExp -> {
            switch (workExp) {
                case "不限":
                    positionInfoMsg.setEducation(EducationEnum.NOT_REQUIRED.getDescription());
                    break;
                case "大专":
                    positionInfoMsg.setEducation(EducationEnum.JUNIOR_COLLEGE.getDescription());
                    break;
                case "本科":
                    positionInfoMsg.setEducation(EducationEnum.UNDERGRADUATE.getDescription());
                    break;
                case "硕士":
                    positionInfoMsg.setEducation(EducationEnum.MASTER.getDescription());
                    break;
                case "博士":
                    positionInfoMsg.setEducation(EducationEnum.DOCTOR.getDescription());
                    break;
                default:
                    positionInfoMsg.setEducation(EducationEnum.NOT_REQUIRED.getDescription());
                    break;
            }
        });
        // 公司发展阶段转换
        OptionalOperationUtils.consumeIfNotBlank(positionInfoMsg.getCompanyDevelopmentalStage(), developmentalStage -> {
            switch (developmentalStage) {
                case "不需要融资":
                    positionInfoMsg.setCompanyDevelopmentalStage(DevelopmentStageEnum.NOT_NEED.getDescription());
                    break;
                case "未融资":
                    positionInfoMsg.setCompanyDevelopmentalStage(DevelopmentStageEnum.NOT.getDescription());
                    break;
                case "天使轮":
                    positionInfoMsg.setCompanyDevelopmentalStage(DevelopmentStageEnum.ANGEL.getDescription());
                    break;
                case "A轮":
                    positionInfoMsg.setCompanyDevelopmentalStage(DevelopmentStageEnum.A.getDescription());
                    break;
                case "B轮":
                    positionInfoMsg.setCompanyDevelopmentalStage(DevelopmentStageEnum.B.getDescription());
                    break;
                case "C轮":
                    positionInfoMsg.setCompanyDevelopmentalStage(DevelopmentStageEnum.C.getDescription());
                    break;
                case "D轮及以上":
                    positionInfoMsg.setCompanyDevelopmentalStage(DevelopmentStageEnum.D.getDescription());
                    break;
                case "已上市":
                    positionInfoMsg.setCompanyDevelopmentalStage(DevelopmentStageEnum.LISTED.getDescription());
                    break;
                default:
                    positionInfoMsg.setCompanyDevelopmentalStage("");
                    break;
            }
        });
    }

    private void handleSearchCondition(Request.Builder queryBuilder, SearchPositionInfoMsg searchCondition) {
        // 搜索内容
        queryBuilder.addQueryStringParameter("query", searchCondition.getContent());
        // 城市
        Byte city = searchCondition.getCity();
        queryBuilder.addQueryStringParameter("city", SearchConditionConverter.getCity(city,
                SearchConditionConverter.SITE_NAME.BOSS));
    }

    private void handleEveryPage(Document document, int pageNum) {
        Elements liElements = document.selectFirst("div[class=job-list]").selectFirst("ul").getElementsByTag("li");
        for (int i = 0; i < liElements.size(); i++) {
            Element liElement = liElements.get(i);
            int index = i + 1;
            log.info("开始处理第{}页第{}条职位信息...", pageNum, index);
            // 获取职位链接
            Element aElement = liElement.selectFirst("div[class=job-primary]")
                    .selectFirst("div[class=info-primary]")
                    .selectFirst("h3[class=name]")
                    .selectFirst("a");
            String positionDetailUrl = BASE_URL + aElement.attr("href");
            // 设置职位唯一标识、来源、URL
            String uniqueKey = aElement.attr("data-jobid");
            PositionInfoMsg positionInfoMsg = new PositionInfoMsg();
            positionInfoMsg.setUniqueKey(uniqueKey);
            positionInfoMsg.setSource(PositionSourceEnum.BOSS.getCode());
            positionInfoMsg.setUrl(positionDetailUrl);
            // 分析详情页
            try {
                log.info("开始解析第{}页第{}条职位信息...", pageNum, index);
                analysisPositionDetail(positionInfoMsg.getUrl(), positionInfoMsg);
            } catch (Exception e) {
                log.error("请求解析职位详情异常！", e);
                continue;
            }

            // 推送MQ
            try {
                log.info("开始推送第{}页第{}条职位信息...", pageNum, i + 1);
                mqProducer.sendPositionInfoMsg(positionInfoMsg);
            } catch (Exception e) {
                log.error("推送职位信息消息到MQ异常！", e);
                continue;
            }

            log.info("第{}页第{}条职位信息处理成功！", pageNum, index);
        }
    }

    private void analysisPositionDetail(String url, PositionInfoMsg positionInfoMsg) throws Exception {
        proxyService.defaultRandomSleep();
        String detailPage = Request.builder().urlNonParams(url).build().getByJsoup();
        Document document = Jsoup.parse(detailPage);
        Element mainElement = document.getElementById("main");

        Element primaryElement = mainElement.selectFirst("div[class=info-primary]");
        // 职位名称
        positionInfoMsg.setName(primaryElement.selectFirst("div[class=name]").selectFirst("h1").text());
        // 薪水
        positionInfoMsg.setSalary(primaryElement.selectFirst("div[class=name]").selectFirst("span[class=salary]").text());

        Element pElement = primaryElement.selectFirst("p");
        List<TextNode> textNodes = pElement.textNodes();
        // 城市
        positionInfoMsg.setCity(textNodes.get(0).text());
        // 工作经验
        positionInfoMsg.setWorkExp(textNodes.get(1).text());
        // 学历
        positionInfoMsg.setEducation(textNodes.get(2).text());

        // 福利
        Elements spanElements = primaryElement.selectFirst("div[class=tag-container]").selectFirst("div[class=job-tags]").getElementsByTag("span");
        String welfare = spanElements.stream().map(Element::text).reduce((s1, s2) -> s1 + "," + s2).orElse("");
        positionInfoMsg.setWelfare(welfare);

        Element companyElement = mainElement.selectFirst("div[class=sider-company]");
        Elements aElements = companyElement.selectFirst("div[class=company-info]").getElementsByTag("a");
        // 公司logo
        positionInfoMsg.setCompanyLogo(aElements.get(0).selectFirst("img").attr("src"));
        // 公司名称
        positionInfoMsg.setCompanyName(aElements.get(1).text());
        // 公司url
        positionInfoMsg.setCompanyUrl(BASE_URL + aElements.get(1).attr("href"));

        Elements pElements = companyElement.getElementsByTag("p");
        // 公司发展阶段
        positionInfoMsg.setCompanyDevelopmentalStage(pElements.get(1).text());
        // 公司规模
        positionInfoMsg.setCompanyScale(pElements.get(2).text());
        // 公司主营领域
        positionInfoMsg.setCompanyDomain(pElements.get(3).text());

        Elements secElements = mainElement.selectFirst("div[class=detail-content]").select("div[class=job-sec]");
        // 职位描述
        positionInfoMsg.setDescription(secElements.get(0).selectFirst("div[class=text]").text().replace("<br>", SysConsts.LINE_SEPARATOR));
        // 工作地址
        positionInfoMsg.setWorkAddress(secElements.last().selectFirst("div[class=job-location]").selectFirst("div[class=location-address]").text());
        // 公司介绍
        positionInfoMsg.setCompanyIntroduction(mainElement.selectFirst("div[class=detail-content]")
                .selectFirst("div[class='job-sec company-info']")
                .selectFirst("div[class=text]").text().replace("<br>", SysConsts.LINE_SEPARATOR));
    }
}
