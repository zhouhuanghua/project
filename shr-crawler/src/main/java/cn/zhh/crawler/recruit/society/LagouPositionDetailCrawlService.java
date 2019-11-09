package cn.zhh.crawler.recruit.society;

import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.enums.DevelopmentStageEnum;
import cn.zhh.common.enums.EducationEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.enums.WorkExpEnum;
import cn.zhh.common.util.JsonUtils;
import cn.zhh.common.util.OptionalOperationUtils;
import org.apache.http.client.utils.DateUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/10 2:09
 */
@Component
public class LagouPositionDetailCrawlService implements PositionDetailCrawlService {

    private static final Pattern UNIQUE_KEY_PATTERN = Pattern.compile("\\/\\d+\\.html");

    private static final Pattern PATTERN1 = Pattern.compile("^\\d{2}:\\d{2}");

    private static final Pattern PATTERN2 = Pattern.compile("^\\d{1}");

    private static final Pattern PATTERN3 = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}");

    @Override
    public Byte webSite() {
        return PositionSourceEnum.LAGOU.getCode();
    }

    @Override
    public boolean isNormalPage(Document detailDocument) {
        return Objects.nonNull(detailDocument.selectFirst("div[class=job-name]"));
    }

    @Override
    public PositionInfoMsg generateObj(String url, Document detailDocument) {
        PositionInfoMsg positionInfoMsg = new PositionInfoMsg();

        // 来源
        positionInfoMsg.setSource(PositionSourceEnum.LAGOU.getCode());

        // 链接
        positionInfoMsg.setUrl(url);

        // 唯一标识
        Matcher matcher = UNIQUE_KEY_PATTERN.matcher(url);
        if (matcher.find()) {
            positionInfoMsg.setUniqueKey(matcher.group().replace("/", "").replace(".html", ""));
        }

        // 职位名称
        String name = detailDocument.selectFirst("div[class=job-name]").attr("title");
        positionInfoMsg.setName(name);

        Elements spanElements = detailDocument.selectFirst("dd[class=job_request]").selectFirst("h3").getElementsByTag("span");
        // 薪水
        positionInfoMsg.setSalary(spanElements.get(0).text().trim());
        // 城市
        positionInfoMsg.setCity(spanElements.get(1).text().replaceAll("\\s*/\\s*", ""));
        // 工作经验
        positionInfoMsg.setWorkExp(spanElements.get(2).text().replaceAll("\\s*/\\s*", ""));
        // 学历
        positionInfoMsg.setEducation(spanElements.get(3).text().replaceAll("\\s*/\\s*", ""));

        // 发布时间
        String publishTimeText = detailDocument.selectFirst("dd[class=job_request]").selectFirst("p[class=publish_time]").text();
        if (StringUtils.hasText(publishTimeText)) {
            positionInfoMsg.setPublishTime(getPublishTime(publishTimeText));
        }

        // 职位标签
        Elements labelElements = detailDocument.selectFirst("dd[class=job_request]").selectFirst("ul").getElementsByTag("li");
        String positionLabel = labelElements.stream().map(Element::text).reduce((s1, s2) -> s1 + "," + s2).orElse("");
        positionInfoMsg.setLabel(positionLabel);

        // 福利
        String advantage = detailDocument.selectFirst("dd[class=job-advantage]").getElementsByTag("p").get(0).text();
        positionInfoMsg.setWelfare(advantage);

        // 职位描述
        String jobDescription = getJobDescription(detailDocument.selectFirst("div[class=job-detail]"));
        positionInfoMsg.setDescription(jobDescription);

        // 工作地址
        Elements aElements = detailDocument.selectFirst("div[class=work_addr]").getElementsByTag("a");
        String workAddress = aElements.stream().map(Element::text).reduce((s1, s2) -> s1 + s2).orElse("");
        positionInfoMsg.setWorkAddress(workAddress.replace("查看地图", ""));

        Element companyElement = detailDocument.getElementById("job_company");
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
            return DateUtils.parseDate(matcher.group(), new String[]{"yyyy-MM-dd"});
        }

        return new Date();
    }

    private String getJobDescription(Element element) {
        Stream<String> lineStream;
        // 只有br标签
        boolean allBr = element.children().stream().allMatch(e -> Objects.equals(e.tagName(), "br"));
        if (allBr) {
            lineStream = Arrays.stream(element.html().split("<br>"));
        }
        // 有p标签
        else {
            lineStream = element.getElementsByTag("p").stream()
                    .map(Element::html)
                    .flatMap(html -> Arrays.stream(html.split("<br>")));
        }
        List<String> lineList = lineStream.map(line ->
                line.replace("\n", "")
                        .replace("&nbsp;", "")
                        .replaceAll("<\\/*[a-zA-Z]+\\/*>", "")
                        .trim()
        )
                .filter(StringUtils::hasText).collect(Collectors.toList());
        return JsonUtils.toJson(lineList);
    }

    @Override
    public void convertWorkExp(PositionInfoMsg positionInfoMsg) {
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
    }

    @Override
    public void convertEducation(PositionInfoMsg positionInfoMsg) {
        OptionalOperationUtils.consumeIfNotBlank(positionInfoMsg.getEducation(), education -> {
            switch (education) {
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
    }

    @Override
    public void convertCompanyDevelopmentalStage(PositionInfoMsg positionInfoMsg) {
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
}
