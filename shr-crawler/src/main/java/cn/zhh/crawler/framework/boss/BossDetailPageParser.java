package cn.zhh.crawler.framework.boss;

import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.enums.PositionSourceEnum;
import cn.zhh.crawler.framework.DetailPageParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class BossDetailPageParser implements DetailPageParser<PositionInfoMsg> {
    @Override
    public DetailPageParser<PositionInfoMsg> newInstance() {
        return new BossDetailPageParser();
    }

    @Override
    public String parseUrl(String baseUrl, Document itemDocument) {
        Element aElement = itemDocument.selectFirst("div[class=info-primary]").selectFirst("h3[class=name]").selectFirst("a");
        return baseUrl + aElement.attr("href");
    }

    @Override
    public boolean isNormalPage(Document detailDocument) {
        return true;
    }

    @Override
    public PositionInfoMsg generateObj(String url, Document detailDocument) {
        PositionInfoMsg positionInfoMsg = new PositionInfoMsg();

        // 来源
        positionInfoMsg.setSource(PositionSourceEnum.BOSS.getCode());
        // url
        positionInfoMsg.setUrl(url);

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
        positionInfoMsg.setCompanyUrl("https://www.zhipin.com/" + aElements.get(1).attr("href"));

        Elements pElements = companyElement.getElementsByTag("p");
        // 公司发展阶段
        positionInfoMsg.setCompanyDevelopmentalStage(pElements.get(1).text());
        // 公司规模
        positionInfoMsg.setCompanyScale(pElements.get(2).text());
        // 公司主营领域
        positionInfoMsg.setCompanyDomain(pElements.get(3).text());

        Elements secElements = mainElement.selectFirst("div[class=detail-content]").select("div[class=job-sec]");
        // 职位描述
        positionInfoMsg.setDescription(secElements.get(0).selectFirst("div[class=text]").text().replace("<br>", SysConsts.LINE_SEPARATOR));
        // 工作地址
        positionInfoMsg.setWorkAddress(secElements.last().selectFirst("div[class=job-location]").selectFirst("div[class=location-address]").text());
        // 公司介绍
        positionInfoMsg.setCompanyIntroduction(mainElement.selectFirst("div[class=detail-content]")
                .selectFirst("div[class='job-sec company-info']")
                .selectFirst("div[class=text]").text().replace("<br>", SysConsts.LINE_SEPARATOR));

        return positionInfoMsg;
    }

    @Override
    public void processObj(PositionInfoMsg obj) {

    }
}
