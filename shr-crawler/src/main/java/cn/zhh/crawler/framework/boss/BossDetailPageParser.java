package cn.zhh.crawler.framework.boss;

import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.enums.DevelopmentStageEnum;
import cn.zhh.common.enums.EducationEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.enums.WorkExpEnum;
import cn.zhh.common.util.JsonUtils;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.crawler.framework.DetailPageParser;
import cn.zhh.crawler.service.MqProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.DateUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BOSS详情页解析器
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class BossDetailPageParser implements DetailPageParser<PositionInfoMsg> {

    private String baseUrl;

    private Pattern publishTimePattern = Pattern.compile("[\\d]{4}-[\\d]{2}-[\\d]{2}\\s[\\d]{2}:[\\d]{2}");

    private Pattern updateTimePattern = Pattern.compile("\\d+月\\d+日");

    @Autowired
    private MqProducer mqProducer;

    @Override
    public String parseUrl(String baseUrl, Document itemDocument) {
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;
        Element aElement = itemDocument.selectFirst("div[class=info-primary]").selectFirst("h3[class=name]").selectFirst("a");
        return this.baseUrl + aElement.attr("href");
    }

    @Override
    public boolean isNormalPage(Document detailDocument) {
        return detailDocument.html().contains("<title>请稍后</title>") ? false : true;
    }

    @Override
    public PositionInfoMsg generateObj(String url, Document detailDocument) {
        PositionInfoMsg positionInfoMsg = new PositionInfoMsg();

        // 来源
        positionInfoMsg.setSource(PositionSourceEnum.BOSS.getCode());

        // 链接
        positionInfoMsg.setUrl(url);

        // 唯一标识
        positionInfoMsg.setUniqueKey(url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".")));

        Element mainElement = detailDocument.getElementById("main");

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
        positionInfoMsg.setCompanyUrl(baseUrl + aElements.get(1).attr("href"));

        Elements pElements = companyElement.getElementsByTag("p");
        // 公司发展阶段
        positionInfoMsg.setCompanyDevelopmentalStage(pElements.get(1).text());
        // 公司规模
        positionInfoMsg.setCompanyScale(pElements.get(2).text());
        // 公司主营领域
        positionInfoMsg.setCompanyDomain(pElements.get(3).text());
        // 发布时间
        String publishTimeText = pElements.get(4).text();
        Matcher matcher = null;
        if ((matcher = publishTimePattern.matcher(publishTimeText)).find()) {
            Date publishTime = DateUtils.parseDate(matcher.group(), new String[]{"yyyy-MM-dd HH:mm"});
            positionInfoMsg.setPublishTime(publishTime);
        } else if ((matcher = updateTimePattern.matcher(publishTimeText)).find()) {
            Date publishTime = DateUtils.parseDate(matcher.group(), new String[]{"yyyy-MM-dd"});
            positionInfoMsg.setPublishTime(publishTime);
        }

        Elements secElements = mainElement.selectFirst("div[class=detail-content]").select("div[class=job-sec]");
        // 职位描述
        String jobDescription = getJobDescription(secElements.get(0));
        positionInfoMsg.setDescription(jobDescription);
        // 工作地址
        positionInfoMsg.setWorkAddress(secElements.last().selectFirst("div[class=job-location]").selectFirst("div[class=location-address]").text());
        // 公司简介
        positionInfoMsg.setCompanyIntroduction(mainElement.selectFirst("div[class=detail-content]")
                .selectFirst("div[class='job-sec company-info']")
                .selectFirst("div[class=text]").text().replace("<br>", SysConsts.LINE_SEPARATOR));

        return positionInfoMsg;
    }

    @Override
    public void processObj(PositionInfoMsg positionInfoMsg) {
        convert(positionInfoMsg);
        mqProducer.sendPositionInfoMsg(positionInfoMsg);
    }

    private String getJobDescription(Element element) {
        String text = element.selectFirst("div[class=text]").html();
        return JsonUtils.toJson(Arrays.asList(text.split("\\n<br>")));
    }

    private void convert(PositionInfoMsg positionInfoMsg) {
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
}
