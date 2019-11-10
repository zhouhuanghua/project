package cn.zhh.crawler.recruit.society;

import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.enums.DevelopmentStageEnum;
import cn.zhh.common.enums.EducationEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.enums.WorkExpEnum;
import cn.zhh.common.util.JsonUtils;
import cn.zhh.common.util.OptionalOperationUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/10 2:15
 */
@Component
public class ZhilianPositionDetailCrawlService implements PositionDetailCrawlService {

    private static final Pattern PUBLISH_TIME_PATTERN1 = Pattern.compile("[\\d]{2}:[\\d]{2}");

    private static final Pattern PUBLISH_TIME_PATTERN2 = Pattern.compile("[\\d]{4}-[\\d]{2}-[\\d]{2}");

    @Override
    public Byte webSite() {
        return PositionSourceEnum.ZHILIAN.getCode();
    }

    @Override
    public boolean isNormalPage(Document detailDocument) {
        return Objects.nonNull(detailDocument.selectFirst("div[class=job-summary]"));
    }

    @Override
    public PositionInfoMsg generateObj(String url, Document detailDocument) {
        PositionInfoMsg positionInfoMsg = new PositionInfoMsg();

        // 来源
        positionInfoMsg.setSource(PositionSourceEnum.ZHILIAN.getCode());

        // 链接
        positionInfoMsg.setUrl(url);

        // 唯一标识
        positionInfoMsg.setUniqueKey(url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".")));

        Element jobElement = detailDocument.selectFirst("div[class=job-summary]");

        // 职位名称
        positionInfoMsg.setName(jobElement.selectFirst("h3[class=summary-plane__title]").text());

        // 发布时间
        String publishTimeText = jobElement.selectFirst("span[class=summary-plane__time]")
                .selectFirst("i[class='iconfont icon-update-time']").text();
        Matcher matcher = null;
        if ((matcher = PUBLISH_TIME_PATTERN1.matcher(publishTimeText)).find()) {
            String[] hourMinute = matcher.group().split(":");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourMinute[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(hourMinute[1]));
            positionInfoMsg.setPublishTime(calendar.getTime());
        } else if ((matcher = PUBLISH_TIME_PATTERN2.matcher(publishTimeText)).find()) {
            String[] monthDay = matcher.group().replace("月", ":")
                    .replace("日", "").split(":");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, Integer.parseInt(monthDay[0]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(monthDay[1]));
            positionInfoMsg.setPublishTime(calendar.getTime());
        }

        // 薪水
        positionInfoMsg.setSalary(jobElement.selectFirst("span[class=summary-plane__salary]").text());

        Elements liElements = jobElement.selectFirst("ul[class=summary-plane__info]").getElementsByTag("li");
        // 城市
        positionInfoMsg.setCity(liElements.get(0).text());
        // 工作经验
        positionInfoMsg.setWorkExp(liElements.get(1).text());
        // 学历
        positionInfoMsg.setEducation(liElements.get(2).text());

        Element jobDetailElement = detailDocument.selectFirst("div[class=job-detail]");
        // 福利
        OptionalOperationUtils.consumeIfNonNull(jobDetailElement.selectFirst("div[class=highlights__content]"), element -> {
            Elements spanElements = element.select("span[class=highlights__content-item]");
            String welfare = spanElements.stream().map(Element::text).reduce((s1, s2) -> s1 + "," + s2).orElse("");
            positionInfoMsg.setWelfare(welfare);
        });
        // 职位描述
        String jobDescription = getJobDescription(jobDetailElement.selectFirst("div[class=describtion]"));
        positionInfoMsg.setDescription(jobDescription);

        // 工作地点
        positionInfoMsg.setWorkAddress(detailDocument.selectFirst("span[class=job-address__content-text]").text());

        Element companyElement = detailDocument.selectFirst("div[class=company]");
        // 公司logo
        OptionalOperationUtils.consumeIfNonNull(companyElement.selectFirst("img[class=company__avatar]"), img -> {
            positionInfoMsg.setCompanyLogo(img.attr("src"));
        });
        // 公司名称
        positionInfoMsg.setCompanyName(companyElement.selectFirst("a[class=company__title]").text());
        // 公司主营领域
        Elements buttonElements = companyElement.selectFirst("div[class=company__detail]").getElementsByTag("button");
        positionInfoMsg.setCompanyDomain(buttonElements.get(0).text());
        // 公司规模
        positionInfoMsg.setCompanyScale(buttonElements.get(1).text());
        // 公司简介
        positionInfoMsg.setCompanyIntroduction(companyElement.selectFirst("div[class=company__description]").text());
        // 公司url
        positionInfoMsg.setCompanyUrl(companyElement.selectFirst("a[class=company__page-site]").text());

        return positionInfoMsg;
    }

    private String getJobDescription(Element element) {
        List<String> lineList = new ArrayList<>();
        OptionalOperationUtils.consumeIfNonNull(element.selectFirst("div[class=describtion__detail-content]"), detail -> {
            String text = detail.html()
                    // <p>段落替换为换行
                    .replaceAll("<p .*?>", SysConsts.LINE_SEPARATOR)
                    // <br><br/>替换为换行
                    .replaceAll("<br\\s*/?>", SysConsts.LINE_SEPARATOR)
                    // 去掉其它的<>之间的东西
                    .replaceAll("\\<.*?>", "");
            lineList.addAll(Arrays.asList(text.split(SysConsts.LINE_SEPARATOR)));
        });
        return JsonUtils.toJson(lineList);
    }

    @Override
    public void convertWorkExp(PositionInfoMsg positionInfoMsg) {
        OptionalOperationUtils.consumeIfNotBlank(positionInfoMsg.getWorkExp(), workExp -> {
            switch (workExp) {
                case "不限":
                    positionInfoMsg.setWorkExp(WorkExpEnum.NOT_REQUIRED.getDescription());
                    break;
                case "无经验":
                    positionInfoMsg.setWorkExp(WorkExpEnum.NONE.getDescription());
                    break;
                case "1年以下":
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
    }

    @Override
    public void convertEducation(PositionInfoMsg positionInfoMsg) {
        OptionalOperationUtils.consumeIfNotBlank(positionInfoMsg.getEducation(), education -> {
            switch (education) {
                case "学历不限":
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
    }

    @Override
    public void convertCompanyDevelopmentalStage(PositionInfoMsg positionInfoMsg) {
        OptionalOperationUtils.consumeIfNotBlank(positionInfoMsg.getCompanyDevelopmentalStage(), developmentalStage -> {
            switch (developmentalStage) {
                case "不需要融资":
                    positionInfoMsg.setCompanyDevelopmentalStage(DevelopmentStageEnum.NOT_NEED.getDescription());
                    break;
                default:
                    positionInfoMsg.setCompanyDevelopmentalStage("");
                    break;
            }
        });
    }
}
