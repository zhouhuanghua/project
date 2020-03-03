package cn.zhh.crawler.util;

import java.util.concurrent.TimeUnit;

/**
 * 睡眠工具类
 *
 * @author Zhou Huanghua
 */
public class SleepUtils {

    private SleepUtils() {
        throw new UnsupportedOperationException("不支持创建实例！");
    }

    public static void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
        }
    }
}
