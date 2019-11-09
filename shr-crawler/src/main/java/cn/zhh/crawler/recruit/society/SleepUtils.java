package cn.zhh.crawler.recruit.society;

import java.util.concurrent.TimeUnit;

/**
 * TODO
 *
 * @author Zhou Huanghua
 * @date 2019/11/9 23:03
 */
public class SleepUtils {

    private SleepUtils() {
    }

    public static void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
        }
    }
}
