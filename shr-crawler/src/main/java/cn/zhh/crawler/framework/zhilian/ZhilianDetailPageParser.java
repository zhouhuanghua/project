package cn.zhh.crawler.framework.zhilian;

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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 智联详情页解析器
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class ZhilianDetailPageParser implements DetailPageParser<PositionInfoMsg> {

    private Pattern publishTimePattern1 = Pattern.compile("[\\d]{2}:[\\d]{2}");

    private Pattern publishTimePattern2 = Pattern.compile("[\\d]{4}-[\\d]{2}-[\\d]{2}");

    @Autowired
    private MqProducer mqProducer;

    @Override
    public String parseUrl(String baseUrl, Document itemDocument) {
        return itemDocument.selectFirst("a[class=contentpile__content__wrapper__item__info]")
                .attr("href");
    }

    @Override
    public boolean isNormalPage(Document detailDocument) {
        return detailDocument.html().contains("为保证您的正常访问，请进行如下验证") ? false : true;
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
        if ((matcher = publishTimePattern1.matcher(publishTimeText)).find()) {
            String[] hourMinute = matcher.group().split(":");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourMinute[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(hourMinute[1]));
            positionInfoMsg.setPublishTime(calendar.getTime());
        } else if ((matcher = publishTimePattern2.matcher(publishTimeText)).find()) {
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
        positionInfoMsg.setWorkAddress(detailDocument.selectFirst("div[class=job-address]").selectFirst("span[class=job-address__content-text]").text());

        Element companyElement = detailDocument.selectFirst("div[class=company]");
        // 公司logo
        positionInfoMsg.setCompanyLogo(companyElement.selectFirst("img[class=company__avatar]").attr("src"));
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

    @Override
    public void processObj(PositionInfoMsg positionInfoMsg) {
        convert(positionInfoMsg);
        mqProducer.sendPositionInfoMsg(positionInfoMsg);
    }

    private String getJobDescription(Element element) {
        List<String> lines = new ArrayList<>();
        // 技能要求
        OptionalOperationUtils.consumeIfNonNull(element.selectFirst("div[class=describtion__skills-content]"), e -> {
            OptionalOperationUtils.consumeIfNotEmpty(e.select("span[class=describtion__skills-item]"), spans -> {
                String skillsStr = spans.stream().map(Element::text).reduce((s1, s2) -> s1 + " " + s2).orElse("");
                lines.add("技能要求:");
                lines.add(skillsStr);
            });
        });
        // 描述
        OptionalOperationUtils.consumeIfNonNull(element.selectFirst("div[class=describtion__detail-content]"), e -> {

            Elements elements = e.select("*");
            List<Element> elementList1 = elements.stream()
                    .filter(e1 -> {
                        Optional<String> any = e1.children().stream().map(Element::tagName).filter(tagName -> !Objects.equals(tagName, "br")).findAny();
                        return !any.isPresent();
                    })
                    .collect(Collectors.toList());
            System.out.println(elementList1);

        });

        return JsonUtils.toJson(null);
    }

    private void convert(PositionInfoMsg positionInfoMsg) {
        // 工作经验转换
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
        // 学历转换
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
        // 公司发展阶段转换
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
