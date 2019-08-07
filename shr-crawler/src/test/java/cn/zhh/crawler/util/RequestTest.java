package cn.zhh.crawler.util;

import org.junit.Before;
import org.junit.Test;

public class RequestTest {

    Request request;

    @Before
    public void before() {
        request = Request.builder().urlNonParams("https://www.lagou.com").build();
    }

    @Test
    public void getByHttpClient() throws Exception {
        System.out.println(request.getByHttpClient());
    }

    @Test
    public void postByHttpClient() throws Exception {
        System.out.println(request.postByHttpClient());
    }

    @Test
    public void getByJsoup() throws Exception {
        System.out.println(request.getByJsoup());
    }

    @Test
    public void postByJsoup() throws Exception {
        System.out.println(request.postByJsoup());
    }

}