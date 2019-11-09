package cn.zhh;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/9 14:04
 */
public class CommonTest {

    @Test
    public void test1() throws IOException {
        String pageContext = new String(Files.readAllBytes(Paths.get("C:\\Users\\dell\\Desktop", "html.txt")));
        Document document = Jsoup.parse(pageContext);
        Element element = document.selectFirst("div[class=detail-content]");
        List<Element> elementList = recursionAllChildren(element);
        elementList.forEach(e -> System.out.println(e.text()));
    }

    private List<Element> recursionAllChildren(Element element) {
        if (Objects.isNull(element)) {
            return Collections.emptyList();
        }
        Elements children = element.children();
        if (children.isEmpty()) {
            return Collections.singletonList(element);
        }
        List<Element> elementList = new ArrayList<>();
        for (Element e : children) {
            elementList.addAll(recursionAllChildren(e));
        }
        return elementList;
    }
}
