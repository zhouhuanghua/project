package cn.zhh.crawler.service;

import cn.zhh.crawler.util.MapUtils;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 代理地址服务接口
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class ProxyService {

    private final CopyOnWriteArrayList<String> proxyAddressList;

    private final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private final String FILE_PATH = "classpath:static/ProxyAddress.txt";

    private final String BASE_XICI_URL = "https://www.xicidaili.com/nn/";

    private final int CRAWLER_MAX_PAGE_NUM = 3;

    private final Map<String, String> COMMON_HEADER_MAP = MapUtils.buildMap(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36",
            "Accept", "application/json, text/plain, */*", "Cookie", "token");

    public ProxyService() {
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

    public void crawl() {
        // 爬取代理地址，并放到集合
        crawl(1);
    }

    private void crawl(int pageNum) {
        log.info("开始爬取第{}页代理地址数据...", pageNum);
        try {
            String page = Request.builder().urlNonParams(BASE_XICI_URL + pageNum).build().getByJsoup();
            Document document = Jsoup.parse(page);
            Elements dataElements = document.getElementById("ip_list")
                    .getElementsByTag("tbody").first()
                    .select("tr[class=odd]");
            // 逐条处理
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
            if (Objects.nonNull(nextPage) && pageNum++ < CRAWLER_MAX_PAGE_NUM) {
                crawl(pageNum);
            }
        } catch (Exception e) {
            log.error("爬取西刺代理地址异常！", e);
        }
    }

    public String getRandomProxyAddress() {
        if (CollectionUtils.isEmpty(proxyAddressList)) {
            return null;
        }
        int nextInt = RANDOM.nextInt(proxyAddressList.size());
        return proxyAddressList.get(nextInt);
    }

    public void sleep(long timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            log.warn("睡眠异常！", e);
        }
    }

    public void randomSleep(long minTimeout, long maxTimeout, TimeUnit timeUnit) {
        try {
            long timeout = RANDOM.nextLong(minTimeout, maxTimeout);
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            log.warn("随机睡眠异常！", e);
        }
    }

    public void defaultRandomSleep() {
        try {
            long timeout = RANDOM.nextLong(1_000, 3_000);
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            log.warn("默认随机睡眠异常！", e);
        }
    }

    public Map<String, String> getCommonHeaderMap(String origin) {
        Map<String, String> headerMap = MapUtils.buildMap("Origin", origin);
        headerMap.putAll(COMMON_HEADER_MAP);
        return headerMap;
    }
}
