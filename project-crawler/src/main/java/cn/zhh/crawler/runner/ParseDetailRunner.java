package cn.zhh.crawler.runner;

import cn.zhh.common.constant.Consts;
import cn.zhh.common.dto.PositionInfo;
import cn.zhh.common.util.JsonUtils;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.crawler.chain.*;
import cn.zhh.crawler.dto.UrlDTO;
import cn.zhh.crawler.mq.MqProducer;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析详情执行器
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class ParseDetailRunner {

    private static final Pattern UNIQUE_KEY_PATTERN = Pattern.compile("\\/\\d+\\.html");

    private static final Pattern PATTERN1 = Pattern.compile("^\\d{2}:\\d{2}");

    private static final Pattern PATTERN2 = Pattern.compile("^\\d{1}");

    private static final Pattern PATTERN3 = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}");

    private static final IStrategy[] STRATEGIES = {new JsoupStrategy(), new JBrowserStrategy(), new ChromeStrategy()};

    @Autowired
    private MqProducer mqProducer;

    public void parseDetail(UrlDTO urlDTO) {
        String url = urlDTO.getUrl();
        ObjectWrapper<Document> docWrapper = new ObjectWrapper();
        CrawlStrategyChain.build(STRATEGIES).doCrawl(url, docWrapper);
        if (docWrapper.nonNull()) {
            parseDetailPage(docWrapper.getObj(), url);
        }
        if (urlDTO.getRetryCount() > 0) {
            mqProducer.sendUrl(new UrlDTO(url, urlDTO.getRetryCount() - 1));
        }
    }

    private void parseDetailPage(Document document, String url) {
        PositionInfo positionInfo = generateObj(document);
        positionInfo.setUrl(url);
        // 唯一标识
        Matcher matcher = UNIQUE_KEY_PATTERN.matcher(url);
        if (matcher.find()) {
            positionInfo.setUniqueKey(matcher.group().replace("/", "").replace(".html", ""));
        }
        mqProducer.sendDetail(positionInfo);
    }

    private PositionInfo generateObj(Document detailDocument) {
        PositionInfo positionInfo = new PositionInfo();

        // 职位名称
        positionInfo.setName(detailDocument.selectFirst("div[class=job-name]").attr("title"));

        Elements spanElements = detailDocument.selectFirst("dd[class=job_request]").selectFirst("h3").getElementsByTag("span");
        // 薪水
        positionInfo.setSalary(spanElements.get(0).text().trim());
        // 城市
        positionInfo.setCity(spanElements.get(1).text().replaceAll("\\s*/\\s*", ""));
        // 工作经验
        positionInfo.setWorkExp(spanElements.get(2).text().replaceAll("\\s*/\\s*", ""));
        // 学历
        positionInfo.setEducation(spanElements.get(3).text().replaceAll("\\s*/\\s*", ""));
        // 发布时间
        String publishTimeText = detailDocument.selectFirst("dd[class=job_request]").selectFirst("p[class=publish_time]").text();
        if (StringUtils.hasText(publishTimeText)) {
            positionInfo.setPublishTime(getPublishTime(publishTimeText));
        }
        // 职位标签
        Elements labelElements = detailDocument.selectFirst("dd[class=job_request]").selectFirst("ul").getElementsByTag("li");
        positionInfo.setLabel(labelElements.stream().map(Element::text).reduce((s1, s2) -> s1 + "," + s2).orElse(""));
        // 福利
        positionInfo.setWelfare(detailDocument.selectFirst("dd[class=job-advantage]").getElementsByTag("p").get(0).text());
        // 职位描述
        positionInfo.setDescription(getJobDescription(detailDocument.selectFirst("dd[class=job_bt]")));
        // 工作地址
        Elements aElements = detailDocument.selectFirst("div[class=work_addr]").getElementsByTag("a");
        String workAddress = aElements.stream().map(Element::text).reduce((s1, s2) -> s1 + s2).orElse("");
        positionInfo.setWorkAddress(workAddress.replace("查看地图", ""));

        Element companyElement = detailDocument.getElementById("job_company");
        Element aElement = companyElement.selectFirst("dt").selectFirst("a");
        // 公司主页
        positionInfo.setCompanyUrl(aElement.attr("href"));

        Element imgElement = aElement.selectFirst("img[class=b2]");
        // 公司名称
        positionInfo.setCompanyName(imgElement.attr("alt"));
        // 公司logo
        positionInfo.setCompanyLogo(imgElement.attr("src"));

        Elements liElements = companyElement.selectFirst("ul[class=c_feature]").getElementsByTag("li");
        // 公司领域
        positionInfo.setCompanyDomain(liElements.get(0).selectFirst("h4[class=c_feature_name]").text());
        // 公司发展阶段
        positionInfo.setCompanyDevelopmentalStage(liElements.get(1).selectFirst("h4[class=c_feature_name]").text());
        // 公司规模
        positionInfo.setCompanyScale(liElements.get(2).selectFirst("h4[class=c_feature_name]").text());
        // 公司介绍 无

        return positionInfo;
    }

    private Date getPublishTime(String text) {
        Matcher matcher = null;
        // 09:58  发布于拉勾网
        if ((matcher = PATTERN1.matcher(text)).find()) {
            String[] hm = matcher.group().split(":");
            int h = Integer.parseInt(hm[0]);
            int m = Integer.parseInt(hm[1]);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            return calendar.getTime();
        }
        // 3天前  发布于拉勾网
        else if ((matcher = PATTERN2.matcher(text)).find()) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -(Integer.parseInt(matcher.group())));
        }
        // 2019-08-08  发布于拉勾网
        else if ((matcher = PATTERN3.matcher(text)).find()) {
            LocalDateTime dateTime = LocalDateTime.parse(matcher.group(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return Date.from(dateTime.toInstant(ZoneOffset.of("+8")));
        }

        return new Date();
    }

    private String getJobDescription(Element element) {
        List<String> lineList = Lists.newArrayList();
        OptionalOperationUtils.consumeIfNonNull(element.selectFirst("div[class=job-detail]"), detail -> {
            String text = detail.html()
                // <p>段落替换为换行
                .replaceAll("<p.*?>", Consts.LINE_SEPARATOR)
                // <br><br/>替换为换行
                .replaceAll("<br\\s*/?>", Consts.LINE_SEPARATOR)
                // 去掉其它的<>之间的东西
                .replaceAll("\\<.*?>", "")
                // 去掉&nbsp;
                .replaceAll("&nbsp;", "");
            Arrays.stream(text.split(Consts.LINE_SEPARATOR)).filter(StringUtils::hasText).forEach(lineList::add);
        });
        return JsonUtils.toJson(lineList);
    }
}
