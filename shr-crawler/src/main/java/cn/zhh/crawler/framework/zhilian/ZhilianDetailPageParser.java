package cn.zhh.crawler.framework.zhilian;

import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.enums.DevelopmentStageEnum;
import cn.zhh.common.enums.EducationEnum;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.common.enums.WorkExpEnum;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.crawler.framework.DetailPageParser;
import cn.zhh.crawler.service.MqProducer;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 智联详情页解析器
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class ZhilianDetailPageParser implements DetailPageParser<PositionInfoMsg> {

    @Autowired
    private MqProducer mqProducer;

    @Override
    public DetailPageParser<PositionInfoMsg> newInstance() {
        return new ZhilianDetailPageParser();
    }

    @Override
    public String parseUrl(String baseUrl, Document itemDocument) {
        return itemDocument.selectFirst("a[class=contentpile__content__wrapper__item__info]")
                .attr("href");
    }

    @Override
    public boolean isNormalPage(Document detailDocument) {
        return true;
    }

    @Override
    public PositionInfoMsg generateObj(String url, Document detailDocument) {
        PositionInfoMsg positionInfoMsg = new PositionInfoMsg();
        https:
//jobs.zhaopin.com/164392122250540.htm
        positionInfoMsg.setSource(PositionSourceEnum.ZHILIAN.getCode());
        positionInfoMsg.setUrl(url);
        positionInfoMsg.setUniqueKey(url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".")));

        Element jobElement = detailDocument.selectFirst("div[class=job-summary]");

        positionInfoMsg.setName(jobElement.selectFirst("h3[class=summary-plane__title]").text());

        positionInfoMsg.setSalary(jobElement.selectFirst("span[class=summary-plane__salary]").text());

        Elements liElements = jobElement.selectFirst("ul[class=summary-plane__info]").getElementsByTag("li");
        positionInfoMsg.setCity(liElements.get(0).text());
        positionInfoMsg.setWorkExp(liElements.get(1).text());
        positionInfoMsg.setEducation(liElements.get(2).text());

        Elements pElements = detailDocument.selectFirst("div[class=job-detail]")
                .selectFirst("div[class=describtion__detail-content]").getElementsByTag("p");
        String description = pElements.stream().map(Element::text).reduce((s1, s2) -> s1 + SysConsts.LINE_SEPARATOR + s2).orElse("");
        positionInfoMsg.setDescription(description);

        positionInfoMsg.setWorkAddress(detailDocument.selectFirst("div[class=job-address]").selectFirst("span[class=job-address__content-text]").text());

        Element companyElement = detailDocument.selectFirst("div[class=company]");
        positionInfoMsg.setCompanyLogo(companyElement.selectFirst("img[class=company__avatar]").attr("src"));
        positionInfoMsg.setCompanyName(companyElement.selectFirst("a[class=company__title]").text());
        Elements buttonElements = companyElement.selectFirst("div[class=company__detail]").getElementsByTag("button");
        positionInfoMsg.setCompanyDomain(buttonElements.get(0).text());
        positionInfoMsg.setCompanyScale(buttonElements.get(1).text());
        positionInfoMsg.setCompanyIntroduction(companyElement.selectFirst("div[class=company__description]").text());
        positionInfoMsg.setCompanyUrl(companyElement.selectFirst("a[class=company__page-site]").text());

        return positionInfoMsg;
    }

    @Override
    public void processObj(PositionInfoMsg positionInfoMsg) {
        convert(positionInfoMsg);
        mqProducer.sendPositionInfoMsg(positionInfoMsg);
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
        OptionalOperationUtils.consumeIfNotBlank(positionInfoMsg.getWorkExp(), workExp -> {
            switch (workExp) {
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
