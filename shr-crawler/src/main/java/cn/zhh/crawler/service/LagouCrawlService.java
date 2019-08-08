package cn.zhh.crawler.service;

import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.crawler.constant.CrawlerConsts;
import cn.zhh.crawler.util.FunctionUtils;
import cn.zhh.crawler.util.ProxyUtils;
import cn.zhh.crawler.util.Request;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
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
    private ProxyAddressService proxyAddressService;

    @Override
    public void crawl(SearchPositionInfoMsg searchCondition) throws Exception {
        // 创建驱动，并设置页面加载超时时间和元素定位超时时间
        System.setProperty("webdriver.chrome.driver", "src/main/resources/static/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        // 窗口最大
        driver.manage().window().maximize();
        // 打开网页
        log.info("开始打开网页...");
        driver.navigate().to("https://www.lagou.com/");
        sleep(5, TimeUnit.SECONDS);
        // 选择城市
        log.info("开始选择城市...");
        selectCity(driver, searchCondition.getCity());
        // 执行搜索
        log.info("开始搜索职位...");
        driver.findElement(By.id("search_input")).sendKeys(searchCondition.getContent());
        driver.findElement(By.id("search_button")).click();
        // 选择工作经验 todo
        log.info("开始选择工作经验...");
        // 选择学历 todo
        log.info("开始选择学历...");
        // 选择公司发展阶段 todo
        log.info("开始选择公司发展阶段...");
        // 选择最新排序方式
        FunctionUtils.consumeIfNonNull(driver.findElement(By.id("order")), orderElement -> {
            log.info("按发布时间排序...");
            orderElement.findElement(By.cssSelector("li[class=wrapper]"))
                    .findElement(By.tagName("div")).findElements(By.tagName("a")).get(1).click();
        });

        sleep(5, TimeUnit.SECONDS);
        // 处理第一页
        int pageNum = 1;
        handleEveryPage(driver, pageNum);
        // 下一页(暂时爬取5页)
        for (; pageNum < 6; ++pageNum) {
            WebElement pagerNextElement = driver.findElement(By.className("pager_next"));
            if (Objects.isNull(pagerNextElement)) {
                return;
            }
            // 点击下一页，等待5秒后开始处理页面
            pagerNextElement.click();
            sleep(5, TimeUnit.SECONDS);
            handleEveryPage(driver, pageNum);
        }

        // 关闭
        FunctionUtils.consumeIfNonNull(driver, WebDriver::quit);
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
            ProxyUtils.randomSleep();
        }
    }

    private void analysisPositionDetail(String positionDetailUrl, PositionInfoMsg positionInfoMsg) throws Exception {
        // 抓取网页内容
        Request.Builder builder = Request.builder().urlNonParams(positionDetailUrl).addHeaders(CrawlerConsts.HEADER_MAP);
        Document document = Jsoup.parse(builder.proxy(proxyAddressService.randomProxyAddress()).build().getByJsoup());
        Element nameElement = null;
        for (int timeout = 30; Objects.isNull(nameElement = document.selectFirst("div[class=job-name]")); timeout+=5) {
            if (timeout > 60) {
                throw new RuntimeException("无法加载页面：" + SysConsts.LINE_SEPARATOR + document.html());
            }
            log.info("睡眠{}秒进行等待...", timeout);
            sleep(timeout, TimeUnit.SECONDS);
            document = Jsoup.parse(builder.proxy(proxyAddressService.randomProxyAddress()).build().getByJsoup());
        }

        // 职位名称
        String name = nameElement.attr("title");
        positionInfoMsg.setName(name);

        Elements spanElements = document.selectFirst("dd[class=job_request]").selectFirst("h3").getElementsByTag("span");
        // 薪水
        positionInfoMsg.setSalary(spanElements.get(0).text().trim());
        // 城市
        positionInfoMsg.setCity(spanElements.get(1).text().trim().replace("/", ""));
        // 工作经验
        positionInfoMsg.setWorkExp(spanElements.get(2).text().trim());
        // 学历
        positionInfoMsg.setEducation(spanElements.get(3).text().trim());

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

    private void selectCity(WebDriver driver, String city) {

        if (StringUtils.hasText(city)) {
            // 城市列表
            List<WebElement> cityElements = driver.findElement(By.id("changeCityBox"))
                .findElement(By.tagName("ul"))
                .findElements(By.tagName("li"));
            // 根据城市点击不同的li标签
            switch (city) {
                case "北京": cityElements.get(0).click(); return;
                case "上海": cityElements.get(1).click(); return;
                case "广州": cityElements.get(3).click(); return;
                case "深圳": cityElements.get(4).click(); return;
                case "杭州": cityElements.get(2).click(); return;
                case "成都": cityElements.get(5).click(); return;
                default: break;
            }
        }
        // 没有匹配的，选择全国
        driver.findElement(By.id("changeCityBox")).findElement(By.cssSelector("p[class=checkTips]")).findElement(By.tagName("a")).click();
    }

    private void sleep(long timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("睡眠异常！", e);
        }
    }
}
