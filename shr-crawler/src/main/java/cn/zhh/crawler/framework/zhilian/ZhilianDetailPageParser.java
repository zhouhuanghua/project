package cn.zhh.crawler.framework.zhilian;

import cn.zhh.common.dto.mq.PositionInfoMsg;
import cn.zhh.common.enums.DevelopmentStageEnum;
import cn.zhh.common.enums.EducationEnum;
import cn.zhh.common.enums.WorkExpEnum;
import cn.zhh.common.util.OptionalOperationUtils;
import cn.zhh.crawler.framework.DetailPageParser;
import cn.zhh.crawler.service.MqProducer;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
@Slf4j
@Component
public class ZhilianDetailPageParser implements DetailPageParser<PositionInfoMsg> {

    private String baseUrl;

    private Pattern publishTimePattern = Pattern.compile("[\\d]{4}-[\\d]{2}-[\\d]{2}\\s[\\d]{2}:[\\d]{2}");

    private Pattern updateTimePattern = Pattern.compile("[\\d]{4}-[\\d]{2}-[\\d]{2}");

    @Autowired
    private MqProducer mqProducer;

    @Override
    public DetailPageParser<PositionInfoMsg> newInstance() {
        return new ZhilianDetailPageParser();
    }

    @Override
    public String parseUrl(String baseUrl, Document itemDocument) {
        this.baseUrl = baseUrl;
        Element aElement = itemDocument.selectFirst("div[class=info-primary]").selectFirst("h3[class=name]").selectFirst("a");
        return baseUrl + aElement.attr("href").substring(1);
    }

    @Override
    public boolean isNormalPage(Document detailDocument) {
        return detailDocument.html().contains("<title>请稍后</title>") ? false : true;
    }

    @Override
    public PositionInfoMsg generateObj(String url, Document detailDocument) {
        PositionInfoMsg positionInfoMsg = new PositionInfoMsg();

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
