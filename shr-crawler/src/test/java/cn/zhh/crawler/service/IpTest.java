package cn.zhh.crawler.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class IpTest {

    @Test
    public void test() throws IOException {
        File file = ResourceUtils.getFile("classpath:ProxyAddress.txt");
        List<String> allLines = Files.readAllLines(file.toPath());
        allLines.forEach(ip -> {
            String[] strs = ip.split(":");
            System.setProperty("http.maxRedirects", "50");
            System.getProperties().setProperty("proxySet", "true");
            System.getProperties().setProperty("http.proxyHost", strs[0]);
            System.getProperties().setProperty("http.proxyPort", strs[1]);
            try {
                Document doc = Jsoup.connect("https://www.baidu.com")
                        .userAgent("Mozilla")
                        .cookie("auth", "token")
                        .timeout(3000)
                        .get();
                if(doc != null) {
                    System.out.println(ip);
                }
            } catch (Throwable t) {

            }
        });
    }
}
