package cn.zhh;

import cn.zhh.common.constant.SysConsts;
import cn.zhh.common.util.JsonUtils;
import cn.zhh.common.util.OptionalOperationUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Document document = Jsoup.parse(pageContext.replaceAll("<br>", SysConsts.LINE_SEPARATOR));
        Element element = document.selectFirst("div[class=describtion]");
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

    @Test
    public void test2() throws IOException {
        String pageContext = new String(Files.readAllBytes(Paths.get("C:\\Users\\dell\\Desktop", "html.txt")));
        FileReader in = new FileReader("C:\\Users\\dell\\Desktop\\html.txt");
        Html2Text parser = new Html2Text();
        parser.parse(in);
        in.close();
        System.out.println(parser.getText());
    }

    @Test
    public void test3() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("C:\\Users\\dell\\Desktop", "html.txt")));
        int len = content.length();
        List<String> contentList = new ArrayList<>(len);
        int begin = 0, end = 0;
        for (int i = 0; i < len; ) {
            if ((content.charAt(i) == '<')
                    && (content.charAt(i + 1) != '!')
                    && (content.charAt(i + 1) != '/')) {
                begin = i;
                for (int j = i + 1; j < len; j++) {
                    if ((content.charAt(j) == ' ') || (content.charAt(j) == '>')) {
                        end = j;
                        i = j + 1;
                        System.out.println(content.substring(begin + 1, end));
                        break;
                    }
                }
            } else {
                i++;
            }
        }
    }

    @Test
    public void test4() throws IOException {

        String content = new String(Files.readAllBytes(Paths.get("C:\\Users\\dell\\Desktop", "html.txt")));
        Pattern pattern = Pattern.compile("^<.*>$");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            System.out.println(matcher.group(1));
        }

    }

    @Test
    public void test5() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("C:\\Users\\dell\\Desktop", "html.txt")));
        Document document = Jsoup.parse(content);
        Element element = document.selectFirst("div[class=describtion]");
        Elements children = element.children();
        children.eachText().forEach(System.out::println);
    }

    @Test
    public void test6() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("C:\\Users\\dell\\Desktop", "html.txt")));
        List<String> lineList = new ArrayList<>();
        Element element = Jsoup.parse(content).selectFirst("div[class=job-sec]");
        OptionalOperationUtils.consumeIfNonNull(element.selectFirst("div[class=text]"), detail -> {
            String text = detail.html()
                    // <p>段落替换为换行
                    .replaceAll("<p .*?>", SysConsts.LINE_SEPARATOR)
                    // <br><br/>替换为换行
                    .replaceAll("<br\\s*/?>", SysConsts.LINE_SEPARATOR)
                    // 去掉其它的<>之间的东西
                    .replaceAll("\\<.*?>", "");
            lineList.addAll(Arrays.asList(text.split(SysConsts.LINE_SEPARATOR)));
        });
        System.out.println(JsonUtils.toJson(lineList));
    }

    public class Html2Text extends HTMLEditorKit.ParserCallback {
        StringBuffer s;

        public Html2Text() {
        }

        public void parse(Reader in) throws IOException {
            s = new StringBuffer();
            ParserDelegator delegator = new ParserDelegator();
            // the third parameter is TRUE to ignore charset directive
            delegator.parse(in, this, Boolean.TRUE);
        }

        public void handleText(char[] text, int pos) {
            s.append(text);
        }

        public String getText() {
            return s.toString();
        }
    }
}
