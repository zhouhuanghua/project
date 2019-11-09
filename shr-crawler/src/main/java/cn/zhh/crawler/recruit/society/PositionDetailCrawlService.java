package cn.zhh.crawler.recruit.society;

import cn.zhh.common.dto.mq.PositionInfoMsg;
import org.jsoup.nodes.Document;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/9 22:31
 */
public interface PositionDetailCrawlService {

    Byte webSite();

    boolean isNormalPage(Document detailDocument);

    PositionInfoMsg generateObj(String url, Document detailDocument);

    void convertWorkExp(PositionInfoMsg positionInfoMsg);

    void convertEducation(PositionInfoMsg positionInfoMsg);

    void convertCompanyDevelopmentalStage(PositionInfoMsg positionInfoMsg);
}
