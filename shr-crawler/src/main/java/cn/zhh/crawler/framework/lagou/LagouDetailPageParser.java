package cn.zhh.crawler.framework.lagou;

import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.crawler.framework.DetailPageParser;
import cn.zhh.crawler.service.MqProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.DateUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class LagouDetailPageParser implements DetailPageParser<PositionInfoMsg> {

    private final Pattern uniqueKeyPattern = Pattern.compile("\\/\\d+\\.html");

    private final Pattern pattern1 = Pattern.compile("^\\d{2}:\\d{2}");

    private final Pattern pattern2 = Pattern.compile("^\\d{1}");

    private final Pattern pattern3 = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}");

    @Autowired
    private MqProducer mqProducer;

    @Override
    public DetailPageParser<PositionInfoMsg> newInstance() {
        return new LagouDetailPageParser();
    }

    @Override
    public String parseUrl(String baseUrl, Document itemDocument) {
        return itemDocument.selectFirst("a[class=position_link]").attr("href");
    }

    @Override
    public PositionInfoMsg generateObj(String url, Document itemDocument) {
        PositionInfoMsg positionInfoMsg = new PositionInfoMsg();

        // 职位来源
        positionInfoMsg.setSource(PositionSourceEnum.LAGOU.getCode());

        // 职位链接
        positionInfoMsg.setUrl(url);

        // 唯一标识
        Matcher matcher = uniqueKeyPattern.matcher(url);
        if (matcher.find()) {
            positionInfoMsg.setUniqueKey(matcher.group().replace("/", "").replace(".html", ""));
        }

        // 职位名称
        String name = itemDocument.selectFirst("div[class=job-name]").attr("title");
        positionInfoMsg.setName(name);

        Elements spanElements = itemDocument.selectFirst("dd[class=job_request]").selectFirst("h3").getElementsByTag("span");
        // 薪水
        positionInfoMsg.setSalary(spanElements.get(0).text().trim());
        // 城市
        positionInfoMsg.setCity(spanElements.get(1).text().replaceAll("\\s*/\\s*", ""));
        // 工作经验
        positionInfoMsg.setWorkExp(spanElements.get(2).text().replaceAll("\\s*/\\s*", ""));
        // 学历
        positionInfoMsg.setEducation(spanElements.get(3).text().replaceAll("\\s*/\\s*", ""));

        // 发布时间
        String publishTimeText = itemDocument.selectFirst("dd[class=job_request]").selectFirst("p[class=publish_time]").text();
        if (StringUtils.hasText(publishTimeText)) {
            positionInfoMsg.setPublishTime(getPublishTime(publishTimeText));
        }

        // 职位标签
        Elements labelElements = itemDocument.selectFirst("dd[class=job_request]").selectFirst("ul").getElementsByTag("li");
        String positionLabel = labelElements.stream().map(Element::text).reduce((s1, s2) -> s1 + "," + s2).orElse("");
        positionInfoMsg.setLabel(positionLabel);

        // 福利
        String advantage = itemDocument.selectFirst("dd[class=job-advantage]").getElementsByTag("p").get(0).text();
        positionInfoMsg.setWelfare(advantage);

        // 职位描述
        Elements pElements = itemDocument.selectFirst("div[class=job-detail]").getElementsByTag("p");
        String desc = pElements.stream().map(Element::text).reduce((s1, s2) -> s1 + SysConsts.LINE_SEPARATOR + s2).orElse("");
        positionInfoMsg.setDescription(desc);

        // 工作地址
        Elements aElements = itemDocument.selectFirst("div[class=work_addr]").getElementsByTag("a");
        String workAddress = aElements.stream().map(Element::text).reduce((s1, s2) -> s1 + s2).orElse("");
        positionInfoMsg.setWorkAddress(workAddress.replace("查看地图", ""));

        Element companyElement = itemDocument.getElementById("job_company");
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

        return positionInfoMsg;
    }

    @Override
    public boolean isNormalPage(Document detailDocument) {
        return Objects.nonNull(detailDocument.selectFirst("div[class=job-name]"));
    }

    @Override
    public void processObj(PositionInfoMsg positionInfoMsg) {
        mqProducer.sendPositionInfoMsg(positionInfoMsg);
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
