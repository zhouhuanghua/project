package cn.zhh.crawler.constant;

import java.util.concurrent.TimeUnit;

/**
 * 常量
 *
 * @author Zhou Huanghua
 */
public interface Consts {

    /** 浏览器打开网页超时时间数值 */
    long BROWSER_OPENPAGE_TIMEOUT_VALUE = 10;

    /** 浏览器打开网页超时时间单位 */
    TimeUnit BROWSER_OPENPAGE_TIMEOUT_UNIT = TimeUnit.SECONDS;

    /** 换行符 */
    String LINE_SEPARATOR = System.lineSeparator();
}
