package cn.zhh.crawler.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
public class JsoupTest {

    @Test
    public void parse() throws IOException {
        File htmlFile = new File("C:\\Users\\dell\\Desktop\\page.txt");
        Reader reader = new FileReader(htmlFile);
        String html = FileCopyUtils.copyToString(reader);
        Document document = Jsoup.parse(html);
        // 职位描述
        /*Element describtionElement = document.select("div[class=describtion]").get(0);
        StringBuilder describtion = new StringBuilder();
        describtion.append("职能要求：").append(SysConsts.LINE_SEPARATOR);
        Element describtionSkill = describtionElement.select("div[class=describtion__skills-content]").get(0);
        describtionSkill.children().forEach(element -> {
            describtion.append(element.text()).append(" ");
        });
        describtion.append(SysConsts.LINE_SEPARATOR);
        Element describtion_Detail = describtionElement.select("div[class=describtion__detail-content]").get(0);
        describtion_Detail.children().forEach(element -> {
            if (element.children().isEmpty()) {
                describtion.append(element.text()).append(SysConsts.LINE_SEPARATOR);
            } else {
                element.children().forEach(e -> {
                    describtion.append(e.text()).append(SysConsts.LINE_SEPARATOR);
                });
            }
        });
        System.out.println(describtion);*/
        // 工作地址
        String workAddress = document.select("span[class=job-address__content-text]").get(0).text();
        System.out.println(workAddress);

        // 公司发展阶段（无）

        // 经营领域
        String companyDomain = document.select("button[class='company__industry']").get(0).text();
        System.out.println(companyDomain);

        // 公司主页
        String companyUrl = document.select("a[class=company__page-site]").get(0).attr("href");
        System.out.println(companyUrl);

        // 公司介绍
        String companyIntroduction = document.select("div[class=company__description]").get(0).text();
        System.out.println(companyIntroduction);
    }
}
