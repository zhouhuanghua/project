package cn.zhh.crawler.service;

import cn.zhh.common.constant.SysConsts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;

/**
 * 请求测试
 *
 * @author Zhou Huanghua
 */
public class RequestTest {

    @Test
    public void testJsoup() throws IOException {
        /*System.setProperty("http.maxRedirects", "50");
        System.getProperties().setProperty("proxySet", "true");
        System.getProperties().setProperty("http.proxyHost", "163.204.245.51");
        System.getProperties().setProperty("http.proxyPort", "9999");*/
        Document document = Jsoup.connect("https://www.baidu.com").proxy("163.204.245.51", 9999).get();
        System.out.println(document);
    }

    @Test
    public void testHttpClient() throws IOException {
        System.setProperty("http.maxRedirects", "50");
        System.getProperties().setProperty("proxySet", "true");
        System.getProperties().setProperty("http.proxyHost", "163.204.245.51");
        System.getProperties().setProperty("http.proxyPort", "9999");
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet("https://www.baidu.com");
        RequestConfig requestConfig = RequestConfig.custom()/*.setProxy(new HttpHost("163.204.245.51", 9999))*/.build();
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();
        String response = EntityUtils.toString(entity, SysConsts.ENCODING);
        System.out.println(response);
    }
}
