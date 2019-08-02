package cn.zhh.crawler.util;

import org.junit.Test;

public class HttpClientUtilsTest {
    @Test
    public void post() throws Exception {
    }

    @Test
    public void get() throws Exception {
        String content = HttpClientUtils.get("http://www.baidu.com", null, null);
        System.out.println(content);
    }

}