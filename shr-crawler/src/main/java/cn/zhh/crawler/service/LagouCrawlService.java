package cn.zhh.crawler.service;

import cn.zhh.common.constant.MqConsts;
import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.*;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.crawler.util.Request;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 拉勾爬虫服务
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class LagouCrawlService implements CrawlService {

    private final String BASE_URL = "https://www.lagou.com/";
    private final Pattern pattern1 = Pattern.compile("^\\d{2}:\\d{2}");
    private final Pattern pattern2 = Pattern.compile("^\\d{1}");
    private final Pattern pattern3 = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}");
    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private ProxyService proxyService;
    @Autowired
    private WebDriverFactory webDriverFactory;

    @Override
    public void crawl(SearchPositionInfoMsg searchCondition) throws Exception {
        WebDriver webDriver = webDriverFactory.openBrowser();
        // 窗口最大
        webDriver.manage().window().maximize();
        // 打开网页
        log.info("开始打开网页...");
        webDriver.navigate().to(BASE_URL);
        proxyService.sleep(5, TimeUnit.SECONDS);
        // 选择城市
        log.info("开始选择城市...");
        selectCity(webDriver, searchCondition.getCity());
        proxyService.sleep(1, TimeUnit.SECONDS);
        // 执行搜索
        log.info("开始搜索职位...");
        webDriver.findElement(By.id("search_input")).sendKeys(searchCondition.getContent());
        webDriver.findElement(By.id("search_button")).click();
        proxyService.sleep(2, TimeUnit.SECONDS);
        // 选择最新排序方式
        OptionalOperationUtils.consumeIfNonNull(webDriver.findElement(By.id("order")), orderElement -> {
            log.info("按发布时间排序...");
            orderElement.findElement(By.cssSelector("li[class=wrapper]"))
                    .findElement(By.tagName("div")).findElements(By.tagName("a")).get(1).click();
        });

        proxyService.sleep(5, TimeUnit.SECONDS);
        // 处理第一页
        int pageNum = 1;
        handleEveryPage(webDriver, pageNum);
        // 下一页(暂时爬取10页)
        for (; ++pageNum < 11; ) {
            WebElement pagerNextElement = webDriver.findElement(By.className("pager_next"));
            if (Objects.isNull(pagerNextElement)) {
                return;
            }
            // 点击下一页，等待5秒后开始处理页面
            pagerNextElement.click();
            proxyService.sleep(5, TimeUnit.SECONDS);
            handleEveryPage(webDriver, pageNum);
        }

        // 关闭
        OptionalOperationUtils.consumeIfNonNull(webDriver, WebDriver::quit);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConsts.SEARCH_POSITION_INFO_LAGOU_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(name = MqConsts.SEARCH_POSITION_INFO_TOPIC_EXCHANGE_NAME, type = "topic"),
            key = ""
    ))
    @RabbitHandler
    @Override
    public void consumeMq(@Payload SearchPositionInfoMsg searchCondition, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        try {
            crawl(searchCondition);
        } catch (Exception e) {
            log.error("拉勾消费职位搜索消息异常！", e);
        }

        // 手动签收消息
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        channel.basicAck(deliveryTag, false);
    }

    @Override
    @Async("asyncServiceExecutor")
    public void syncCrawl(SearchPositionInfoMsg searchCondition) {
        try {
            log.info("拉勾爬虫服务异步启动，职位：{}，城市：{}", searchCondition.getContent(), searchCondition.getCity());
            this.crawl(searchCondition);
        } catch (Exception e) {
            log.error("异步执行拉勾爬虫服务异常！", e);
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
                case "经验应届毕业生":
                    positionInfoMsg.setWorkExp(WorkExpEnum.NONE.getDescription());
                    break;
                case "经验1年以下":
                    positionInfoMsg.setWorkExp(WorkExpEnum.LESS1.getDescription());
                    break;
                case "经验1-3年":
                    positionInfoMsg.setWorkExp(WorkExpEnum.ONE2THREE.getDescription());
                    break;
                case "经验3-5年":
                    positionInfoMsg.setWorkExp(WorkExpEnum.THREE2FIVE.getDescription());
                    break;
                case "经验5-10年":
                    positionInfoMsg.setWorkExp(WorkExpEnum.FIVE2TEN.getDescription());
                    break;
                case "经验10年以上":
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
                case "学历不限":
                    positionInfoMsg.setEducation(EducationEnum.NOT_REQUIRED.getDescription());
                    break;
                case "大专及以上":
                    positionInfoMsg.setEducation(EducationEnum.JUNIOR_COLLEGE.getDescription());
                    break;
                case "本科及以上":
                    positionInfoMsg.setEducation(EducationEnum.UNDERGRADUATE.getDescription());
                    break;
                case "硕士及以上":
                    positionInfoMsg.setEducation(EducationEnum.MASTER.getDescription());
                    break;
                case "博士及以上":
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
                case "上市公司":
                    positionInfoMsg.setCompanyDevelopmentalStage(DevelopmentStageEnum.LISTED.getDescription());
                    break;
                default:
                    positionInfoMsg.setCompanyDevelopmentalStage("");
                    break;
            }
        });
    }

    private void handleEveryPage(WebDriver webDriver, int pageNum) {
        // 获取职位列表
        log.info("开始爬取职位列表...");
        List<WebElement> positionListElement = webDriver.findElement(By.id("s_position_list")).findElement(By.cssSelector("ul[class=item_con_list]"))
                .findElements(By.tagName("li"));
        for (int i = 0; i < positionListElement.size(); i++) {
            WebElement positionElement = positionListElement.get(i);
            PositionInfoMsg positionInfoMsg = new PositionInfoMsg();
            // 处理职位
            try {
                log.info("正在解析第{}页第{}条职位信息...", pageNum, i + 1);
                // 唯一标识
                positionInfoMsg.setUniqueKey(positionElement.getAttribute("data-positionid"));
                // 职位链接
                String href = positionElement.findElement(By.cssSelector("a[class=position_link]")).getAttribute("href");
                positionInfoMsg.setUrl(href);
                // 职位来源
                positionInfoMsg.setSource(PositionSourceEnum.LAGOU.getCode());
                // 解析职位详情
                analysisPositionDetail(href, positionInfoMsg);
            } catch (Exception e) {
                log.error(String.format("职位%s解析异常！positionDetailUrl=%s", positionInfoMsg.getUniqueKey(), positionInfoMsg.getUrl()), e);
                continue;
            }
            // 推送MQ
            try {
                log.info("正在推送第{}页第{}条职位信息...", pageNum, i + 1);
                mqProducer.sendPositionInfoMsg(positionInfoMsg);
            } catch (Exception e) {
                log.error(String.format("职位%s推送MQ异常！", positionInfoMsg.getUniqueKey()), e);
                continue;
            }

            log.info("第{}页第{}条职位信息处理成功！", pageNum, i + 1);
            proxyService.defaultRandomSleep();
        }
    }

    private void analysisPositionDetail(String positionDetailUrl, PositionInfoMsg positionInfoMsg) throws Exception {
        // 抓取网页内容
        Request.Builder builder = Request.builder().urlNonParams(positionDetailUrl).addHeaders(proxyService.getCommonHeaderMap(BASE_URL));
        Document document = Jsoup.parse(builder.proxy(proxyService.getRandomProxyAddress()).build().getByJsoup());
        Element nameElement = null;
        for (int timeout = 60; Objects.isNull(nameElement = document.selectFirst("div[class=job-name]")); timeout += 5) {
            if (timeout > 100) {
                throw new RuntimeException("无法加载页面：" + SysConsts.LINE_SEPARATOR + document.html());
            }
            log.info("睡眠{}秒进行等待...", timeout);
            proxyService.sleep(timeout, TimeUnit.SECONDS);
            document = Jsoup.parse(builder.proxy(proxyService.getRandomProxyAddress()).build().getByJsoup());
        }

        // 职位名称
        String name = nameElement.attr("title");
        positionInfoMsg.setName(name);

        Elements spanElements = document.selectFirst("dd[class=job_request]").selectFirst("h3").getElementsByTag("span");
        // 薪水
        positionInfoMsg.setSalary(spanElements.get(0).text().trim());
        // 城市
        positionInfoMsg.setCity(spanElements.get(1).text().replaceAll("\\s*/\\s*", ""));
        // 工作经验
        positionInfoMsg.setWorkExp(spanElements.get(2).text().replaceAll("\\s*/\\s*", ""));
        // 学历
        positionInfoMsg.setEducation(spanElements.get(3).text().replaceAll("\\s*/\\s*", ""));

        // 发布时间
        String publishTimeText = document.selectFirst("dd[class=job_request]").selectFirst("p[class=publish_time]").text();
        if (StringUtils.hasText(publishTimeText)) {
            positionInfoMsg.setPublishTime(getPublishTime(publishTimeText));
        }

        // 职位标签
        Elements labelElements = document.selectFirst("dd[class=job_request]").selectFirst("ul").getElementsByTag("li");
        String positionLabel = labelElements.stream().map(Element::text).reduce((s1, s2) -> s1 + "," + s2).orElse("");
        positionInfoMsg.setLabel(positionLabel);

        // 福利
        String advantage = document.selectFirst("dd[class=job-advantage]").getElementsByTag("p").get(0).text();
        positionInfoMsg.setWelfare(advantage);

        // 职位描述
        Elements pElements = document.selectFirst("div[class=job-detail]").getElementsByTag("p");
        String desc = pElements.stream().map(Element::text).reduce((s1, s2) -> s1 + SysConsts.LINE_SEPARATOR + s2).orElse("");
        positionInfoMsg.setDescription(desc);

        // 工作地址
        Elements aElements = document.selectFirst("div[class=work_addr]").getElementsByTag("a");
        String workAddress = aElements.stream().map(Element::text).reduce((s1, s2) -> s1 + s2).orElse("");
        positionInfoMsg.setWorkAddress(workAddress.replace("查看地图", ""));

        Element companyElement = document.getElementById("job_company");
        Element aElement = companyElement.selectFirst("dt").selectFirst("a");
        // 公司主页
        positionInfoMsg.setCompanyUrl(aElement.attr("href"));

        Element imgElement = aElement.selectFirst("img[class=b2]");
        // 公司名称
        positionInfoMsg.setCompanyName(imgElement.attr("alt"));
        // 公司logo
        positionInfoMsg.setCompanyLogo(imgElement.attr("src"));

        Elements liElements = companyElement.selectFirst("ul[class=c_feature]").getElementsByTag("li");
        // 公司领域
        positionInfoMsg.setCompanyDomain(liElements.get(0).selectFirst("h4[class=c_feature_name]").text());
        // 公司发展阶段
        positionInfoMsg.setCompanyDevelopmentalStage(liElements.get(1).selectFirst("h4[class=c_feature_name]").text());
        // 公司规模
        positionInfoMsg.setCompanyScale(liElements.get(2).selectFirst("h4[class=c_feature_name]").text());

        // 公司介绍 无
    }

    private void selectCity(WebDriver webDriver, Byte city) {
        // 遍历选择城市里面的全部a标签，构成映射关系
        List<WebElement> aElements = webDriver.findElement(By.id("changeCityBox")).findElements(By.tagName("a"));
        Map<Byte, WebElement> cityMap = new HashMap<>(8);
        for (WebElement aElement : aElements) {
            String text = aElement.getText().trim();
            switch (text) {
                case "全国站":
                    cityMap.put(CityEnum.ALL.getCode(), aElement);
                    break;
                case "北京站":
                    cityMap.put(CityEnum.BEIJING.getCode(), aElement);
                    break;
                case "上海站":
                    cityMap.put(CityEnum.SHANGHAI.getCode(), aElement);
                    break;
                case "广州站":
                    cityMap.put(CityEnum.GUANGZHOU.getCode(), aElement);
                    break;
                case "深圳站":
                    cityMap.put(CityEnum.SHENZHEN.getCode(), aElement);
                    break;
                case "杭州站":
                    cityMap.put(CityEnum.HANGZHOU.getCode(), aElement);
                    break;
                case "成都站":
                    cityMap.put(CityEnum.CHENGDU.getCode(), aElement);
                    break;
                default:
                    break;
            }
        }
        // 如果为空，或者不在映射集合里面，则点击全国站
        if (Objects.isNull(city) || !cityMap.containsKey(city)) {
            cityMap.get(CityEnum.ALL.getCode()).click();
        }
        // 按值点击对应城市
        cityMap.get(city).click();
    }

    private Date getPublishTime(String text) {
        Matcher matcher = null;
        // 09:58  发布于拉勾网
        if ((matcher = pattern1.matcher(text)).find()) {
            String[] hm = matcher.group().split(":");
            int h = Integer.parseInt(hm[0]);
            int m = Integer.parseInt(hm[1]);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            return calendar.getTime();
        }
        // 3天前  发布于拉勾网
        else if ((matcher = pattern2.matcher(text)).find()) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -(Integer.parseInt(matcher.group())));
        }
        // 2019-08-08  发布于拉勾网
        else if ((matcher = pattern3.matcher(text)).find()) {
            return DateUtils.parseDate(matcher.group(), new String[]{"yyyy-MM-dd"});
        }

        return new Date();
    }
}
