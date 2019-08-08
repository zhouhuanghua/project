package cn.zhh.crawler.service;

import cn.zhh.crawler.util.Request;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 代理地址爬取服务接口
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class ProxyAddressService {

    private final CopyOnWriteArrayList<String> proxyAddressList ;
    private final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private final String FILE_PATH = "classpath:static/ProxyAddress.txt";

    public ProxyAddressService() {
        proxyAddressList = new CopyOnWriteArrayList<>();
        loadProxyAddressFile();
    }

    private void loadProxyAddressFile() {
        // 加载文件数据
        try {
            File file = ResourceUtils.getFile(FILE_PATH);
            proxyAddressList.addAll(Files.readAllLines(file.toPath()));
        } catch (IOException e) {
            log.error("加载代理地址文件异常！", e);
        }
    }

    public void cralw(int pageNum) {
        log.info("开始爬取第{}页代理地址数据...", pageNum);
        try {
            String page = Request.builder().urlNonParams("https://www.xicidaili.com/nn/" + pageNum).build().getByJsoup();
            Document document = Jsoup.parse(page);
            Elements dataElements = document.getElementById("ip_list")
                    .getElementsByTag("tbody").first()
                    .select("tr[class=odd]");
            for (Element dataElement : dataElements) {
                Elements tdElements = dataElement.getElementsByTag("td");
                String proxyAddress = tdElements.get(1).text() + ":" + tdElements.get(2).text();
                if (proxyAddressList.contains(proxyAddress)) {
                    continue;
                }
                proxyAddressList.add(proxyAddress);
            }
            log.info("第{}页代理地址数据爬取完成！", pageNum);

            // 递归处理下一页
            Element nextPage = document.select("a[class=next_page]").first();
            if (Objects.nonNull(nextPage) && pageNum++ < 3) {
                cralw(pageNum);
            }

            // 将结果保存到代理地址文件
            upateToFile();

        } catch (Exception e) {
            log.error("爬取代理地址异常！", e);
        }
    }

    private void upateToFile() {
        log.info("开始同步代理地址数据到文件...");
        try {
            File file = ResourceUtils.getFile(FILE_PATH);
            Files.write(file.toPath(), proxyAddressList, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("同步代理地址数据到文件成功！");
        } catch (IOException e) {
            log.error("将集合的代理地址写入文件异常！");
        }
    }

    public String randomProxyAddress() {
        if (CollectionUtils.isEmpty(proxyAddressList)) {
            return null;
        }
        int nextInt = RANDOM.nextInt(proxyAddressList.size());
        return proxyAddressList.get(nextInt);
    }
}
