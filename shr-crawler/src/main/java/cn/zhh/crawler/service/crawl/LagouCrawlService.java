package cn.zhh.crawler.service.crawl;

import cn.zhh.common.constant.MqConsts;
import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.*;
import cn.zhh.crawler.service.MqProducer;
import cn.zhh.crawler.service.ProxyService;
import cn.zhh.crawler.util.OptionalOperationUtils;
import cn.zhh.crawler.util.Request;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 拉勾爬虫服务
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class LagouCrawlService implements CrawlService {
    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private ProxyService proxyService;
    private final String DRIVER_PATH = "src/main/resources/static/chromedriver.exe";
    private final String BASE_URL = "https://www.lagou.com/";

    @Override
    public void crawl(SearchPositionInfoMsg searchCondition) throws Exception {
        // 创建驱动，并设置页面加载超时时间和元素定位超时时间
        System.setProperty("webdriver.chrome.driver", DRIVER_PATH);
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        // 窗口最大
        driver.manage().window().maximize();
        // 打开网页
        log.info("开始打开网页...");
        driver.navigate().to(BASE_URL);
        proxyService.sleep(5, TimeUnit.SECONDS);
        // 选择城市
        log.info("开始选择城市...");
        selectCity(driver, searchCondition.getCity());
        // 执行搜索
        log.info("开始搜索职位...");
        driver.findElement(By.id("search_input")).sendKeys(searchCondition.getContent());
        driver.findElement(By.id("search_button")).click();
        // 选择工作经验
        log.info("开始选择工作经验...");
        selectWorkExp(driver, searchCondition.getWorkExp());
        // 选择学历
        log.info("开始选择学历...");
        selectEducation(driver, searchCondition.getEducation());
        // 选择公司发展阶段
        log.info("开始选择公司发展阶段...");
        selectDevelopmentStage(driver, searchCondition.getDevelopmentStage());
        // 选择最新排序方式
        OptionalOperationUtils.consumeIfNonNull(driver.findElement(By.id("order")), orderElement -> {
            log.info("按发布时间排序...");
            orderElement.findElement(By.cssSelector("li[class=wrapper]"))
                    .findElement(By.tagName("div")).findElements(By.tagName("a")).get(1).click();
        });

        proxyService.sleep(5, TimeUnit.SECONDS);
        // 处理第一页
        int pageNum = 1;
        handleEveryPage(driver, pageNum);
        // 下一页(暂时爬取5页)
        for (; ++pageNum < 6; ) {
            WebElement pagerNextElement = driver.findElement(By.className("pager_next"));
            if (Objects.isNull(pagerNextElement)) {
                return;
            }
            // 点击下一页，等待5秒后开始处理页面
            pagerNextElement.click();
            proxyService.sleep(5, TimeUnit.SECONDS);
            handleEveryPage(driver, pageNum);
        }

        // 关闭
        OptionalOperationUtils.consumeIfNonNull(driver, WebDriver::quit);
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

    private void handleEveryPage(WebDriver driver, int pageNum) {
        // 获取职位列表
        log.info("开始爬取职位列表...");
        List<WebElement> positionListElement = driver.findElement(By.id("s_position_list")).findElement(By.cssSelector("ul[class=item_con_list]"))
                .findElements(By.tagName("li"));
        for (int i = 0; i < positionListElement.size(); i++) {
            WebElement positionElement = positionListElement.get(i);
            PositionInfoMsg positionInfoMsg = new PositionInfoMsg();
            // 处理职位
            try {
                log.info("正在解析第{}页第{}条职位信息...", pageNum ,i+1);
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
                log.info("正在推送第{}页第{}条职位信息...", pageNum ,i+1);
                mqProducer.sendPositionInfoMsg(positionInfoMsg);
            } catch (Exception e) {
                log.error(String.format("职位%s推送MQ异常！", positionInfoMsg.getUniqueKey()), e);
                continue;
            }

            log.info("第{}页第{}条职位信息处理成功！", pageNum ,i+1);
            proxyService.defaultRandomSleep();
        }
    }

    private void analysisPositionDetail(String positionDetailUrl, PositionInfoMsg positionInfoMsg) throws Exception {
        // 抓取网页内容
        Request.Builder builder = Request.builder().urlNonParams(positionDetailUrl).addHeaders(proxyService.getCommonHeaderMap(BASE_URL));
        Document document = Jsoup.parse(builder.proxy(proxyService.getRandomProxyAddress()).build().getByJsoup());
        Element nameElement = null;
        for (int timeout = 30; Objects.isNull(nameElement = document.selectFirst("div[class=job-name]")); timeout+=5) {
            if (timeout > 60) {
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
        positionInfoMsg.setWorkAddress(workAddress);

        // 发布时间 todo

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

    private void selectCity(WebDriver driver, Byte city) {
        // 遍历选择城市里面的全部a标签，构成映射关系
        List<WebElement> aElements = driver.findElement(By.id("changeCityBox")).findElements(By.tagName("a"));
        Map<Byte, WebElement> cityMap = new HashMap<>(8);
        for (WebElement aElement : aElements) {
            String text = aElement.getText().trim();
            switch (text) {
                case "全国站": cityMap.put(CityEnum.ALL.getCode(), aElement); break;
                case "北京站": cityMap.put(CityEnum.BEIJING.getCode(), aElement); break;
                case "上海站": cityMap.put(CityEnum.SHANGHAI.getCode(), aElement); break;
                case "广州站": cityMap.put(CityEnum.GUANGZHOU.getCode(), aElement); break;
                case "深圳站": cityMap.put(CityEnum.SHENZHEN.getCode(), aElement); break;
                case "杭州站": cityMap.put(CityEnum.HANGZHOU.getCode(), aElement); break;
                case "成都站": cityMap.put(CityEnum.CHENGDU.getCode(), aElement); break;
                default: break;
            }
        }
        // 如果为空，或者不在映射集合里面，则点击全国站
        if (Objects.isNull(city) || !cityMap.containsKey(city)) {
            cityMap.get(CityEnum.ALL.getCode()).click();
        }
        // 按值点击对应城市
        cityMap.get(city).click();
    }

    private void selectWorkExp(WebDriver driver, Byte workExp) {
        // 为空或者不限，就不管
        if (Objects.isNull(workExp) || Objects.equals(WorkExpEnum.ALL.getCode(), workExp)) {
            return;
        }
        // 遍历工作经验里面的全部a标签，构成映射关系
        List<WebElement> aElements = driver.findElement(By.id("filterCollapse"))
            .findElements(By.cssSelector("li[class=multi-chosen]")).get(0).findElements(By.tagName("a"));
        Map<Byte, WebElement> workExpMap = new HashMap<>(8);
        for (WebElement aElement : aElements) {
            String text = aElement.getText().trim();
            switch (text) {
                case "不限": workExpMap.put(WorkExpEnum.ALL.getCode(), aElement); break;
                case "应届毕业生": workExpMap.put(WorkExpEnum.NONE.getCode(), aElement); break;
                case "3年及以下": workExpMap.put(WorkExpEnum.ONE2THREE.getCode(), aElement); break;
                case "3-5年": workExpMap.put(WorkExpEnum.THREE2FIVE.getCode(), aElement); break;
                case "5-10年": workExpMap.put(WorkExpEnum.FIVE2TEN.getCode(), aElement); break;
                case "10年以上": workExpMap.put(WorkExpEnum.MORE10.getCode(), aElement); break;
                case "不要求": workExpMap.put(WorkExpEnum.NOT_REQUIRED.getCode(), aElement); break;
                default: break;
            }
        }
        // 不在映射集合里面，则啥也不做
        if (!workExpMap.containsKey(workExp)) {
            return;
        }
        // 按值点击对应工作经验
        workExpMap.get(workExp).click();
    }

    private void selectEducation(WebDriver driver, Byte education) {
        // 为空或者不限，就不管
        if (Objects.isNull(education) || Objects.equals(EducationEnum.ALL.getCode(), education)) {
            return;
        }
        // 遍历工作经验里面的全部a标签，构成映射关系
        System.out.println(driver.getPageSource());
        List<WebElement> aElements = driver.findElement(By.id("filterCollapse"))
                .findElements(By.cssSelector("li[class=multi-chosen]")).get(1).findElements(By.tagName("a"));
        Map<Byte, WebElement> educationExpMap = new HashMap<>(8);
        for (WebElement aElement : aElements) {
            String text = aElement.getText().trim();
            switch (text) {
                case "不限": educationExpMap.put(EducationEnum.ALL.getCode(), aElement); break;
                case "大专": educationExpMap.put(EducationEnum.JUNIOR_COLLEGE.getCode(), aElement); break;
                case "本科": educationExpMap.put(EducationEnum.UNDERGRADUATE.getCode(), aElement); break;
                case "硕士": educationExpMap.put(EducationEnum.MASTER.getCode(), aElement); break;
                case "博士": educationExpMap.put(EducationEnum.DOCTOR.getCode(), aElement); break;
                case "不要求": educationExpMap.put(EducationEnum.NOT_REQUIRED.getCode(), aElement); break;
                default: break;
            }
        }

        // 不在映射集合里面，则啥也不做
        if (!educationExpMap.containsKey(education)) {
            return;
        }

        // 按值点击对应工作经验
        educationExpMap.get(education).click();
    }

    private void selectDevelopmentStage(WebDriver driver, Byte developmentStage) {
        // 为空或者不限，就不管
        if (Objects.isNull(developmentStage) || Objects.equals(DevelopmentStageEnum.ALL.getCode(), developmentStage)) {
            return;
        }
        // 遍历工作经验里面的全部a标签，构成映射关系
        List<WebElement> aElements = driver.findElement(By.id("filterCollapse"))
                .findElements(By.cssSelector("li[class=multi-chosen]")).get(2).findElements(By.tagName("a"));
        Map<Byte, WebElement> developmentStageMap = new HashMap<>(8);
        for (WebElement aElement : aElements) {
            String text = aElement.getText().trim();
            switch (text) {
                case "不限": developmentStageMap.put(DevelopmentStageEnum.ALL.getCode(), aElement); break;
                case "未融资": developmentStageMap.put(DevelopmentStageEnum.NOT.getCode(), aElement); break;
                case "天使轮": developmentStageMap.put(DevelopmentStageEnum.ANGEL.getCode(), aElement); break;
                case "A轮": developmentStageMap.put(DevelopmentStageEnum.A.getCode(), aElement); break;
                case "B轮": developmentStageMap.put(DevelopmentStageEnum.B.getCode(), aElement); break;
                case "C轮": developmentStageMap.put(DevelopmentStageEnum.C.getCode(), aElement); break;
                case "D轮及以上": developmentStageMap.put(DevelopmentStageEnum.D.getCode(), aElement); break;
                case "上市公司": developmentStageMap.put(DevelopmentStageEnum.LISTED.getCode(), aElement); break;
                case "不需要融资": developmentStageMap.put(DevelopmentStageEnum.NOT_NEED.getCode(), aElement); break;
                default: break;
            }
        }

        // 不在映射集合里面，则啥也不做
        if (!developmentStageMap.containsKey(developmentStage)) {
            return;
        }

        // 按值点击对应工作经验
        developmentStageMap.get(developmentStage).click();
    }
}
