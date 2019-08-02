package cn.zhh.crawler.util;

import org.junit.Test;

public class HttpClientUtilsTest {
    @Test
    public void post() throws Exception {
    }

    @Test
    public void get() throws Exception {
        String content = HttpClientUtils.get("https://jobs.zhaopin.com/CC666178323J00154607310.htm", null, null);
        System.out.println(content);
    }

}