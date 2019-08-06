package cn.zhh.crawler.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author Zhou Huanghua
 */
@Slf4j
public class ProxyUtils {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private static List<String> proxyAddressList ;
    static {
        // 加载IP地址
        try {
            File file = ResourceUtils.getFile("classpath:ProxyAddress.txt");
            proxyAddressList = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            log.error("加载代理地址文件异常！", e);
        }

    }


    public static String randomProxyAddress() {
        if (CollectionUtils.isEmpty(proxyAddressList)) {
            return null;
        }
        int nextInt = RANDOM.nextInt(proxyAddressList.size());
        return proxyAddressList.get(nextInt);
    }

    public static void randomSleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(3_000));
        } catch (InterruptedException e) {
            log.warn("随意睡眠异常！", e);
        }
    }
}
