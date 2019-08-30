package cn.zhh.crawler.framework.skill;

import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.dto.mq.SkillMsg;
import cn.zhh.common.util.ThrowableUtils;
import cn.zhh.crawler.framework.DetailPageParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 高博详情页解析器
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class GaoboDetailPageParser implements DetailPageParser<SkillMsg> {

    private final Pattern urlPattern = Pattern.compile("\\/skills\\/\\d*.html");

    private final String fileDir = "C:\\Users\\SI-GZ-1766\\Desktop\\skill\\";

    private final Pattern fileNamePattern = Pattern.compile("[\\/:*?\"<>|]");

    @Override
    public String parseUrl(String baseUrl, Document itemDocument) {
        Element spanElement = itemDocument.selectFirst("div[class=table_list_l]").getElementsByTag("span").last();
        String onclickAttr = spanElement.attr("onclick");
        Matcher matcher = urlPattern.matcher(onclickAttr);
        if (matcher.find()) {
            String simpleUrl = matcher.group();
            return baseUrl.replace("/skills.html", simpleUrl);
        }
        return null;
    }

    @Override
    public boolean isNormalPage(Document detailDocument) {
        return true;
    }

    @Override
    public SkillMsg generateObj(String url, Document detailDocument) {
        SkillMsg skill = new SkillMsg();
        Element detailElement = detailDocument.selectFirst("div[class=news_detail]");

        // 标题
        String title = detailElement.selectFirst("div[class=news_head]").selectFirst("h1").text();
        skill.setTitle(title);

        // 类型
        skill.setType("");

        // 正文
        Element contentElement = detailElement.selectFirst("div[class=news_con]");
        skill.setText(contentElement.text());

        return skill;
    }

    @Override
    public void processObj(SkillMsg skillMsg) {
        String fileName = fileNamePattern.matcher(skillMsg.getTitle()).replaceAll("");
        Path path = Paths.get(fileDir, fileName + ".txt");
        try {
            Files.write(path, skillMsg.getText().getBytes(SysConsts.ENCODING), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            log.error("文章《{}》写入文件异常！e={}", skillMsg.getTitle(), ThrowableUtils.getThrowableStackTrace(e));
        }
    }
}
