package cn.zhh.crawler.util;

import cn.zhh.common.constant.SysConsts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HttpClient工具类
 *
 * @author Zhou Huanghua
 */
public class HttpClientUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static String post(String url, Map<String, String> data, Map<String, String> headers) throws Exception {
        HttpPost httpPost = new HttpPost(url);

        // 设置请求参数
        if (!CollectionUtils.isEmpty(data)) {
            List<NameValuePair> paramList = new LinkedList();
            data.forEach((k, v) -> {
                BasicNameValuePair param = new BasicNameValuePair(k, v);
                paramList.add(param);
            });
            httpPost.setEntity(new UrlEncodedFormEntity(paramList, SysConsts.ENCODING));
        }

        return request(httpPost, headers);
    }

    public static String get(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
        // 根据参数构建请求URL
        URIBuilder uriBuilder = new URIBuilder(url);
        if (!CollectionUtils.isEmpty(params)) {
            params.forEach(uriBuilder::addParameter);
        }
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        return request(httpGet, headers);
    }

    private static String request(HttpRequestBase httpRequest, Map<String, String> headers) throws IOException {
        // 配置信息
        RequestConfig requestConfig = RequestConfig.custom()
                // 设置连接超时时间(单位毫秒)
                .setConnectTimeout(5_000)
                // 设置请求超时时间(单位毫秒)
                .setConnectionRequestTimeout(5_000)
                // socket读写超时时间(单位毫秒)
                .setSocketTimeout(5_000)
                // 设置是否允许重定向(默认为true)
                .setRedirectsEnabled(true).build();
        // 将上面的配置信息运用到请求里
        httpRequest.setConfig(requestConfig);

        // 设置请求头
        if (Objects.nonNull(headers)) {
            headers.forEach(httpRequest::addHeader);
        }

        // 设置代理地址
//        setProxyAddress();

        // 发起请求
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .build()) {
            CloseableHttpResponse httpResponse = httpClient.execute(httpRequest);
            HttpEntity entity = httpResponse.getEntity();
            String response = EntityUtils.toString(entity, SysConsts.ENCODING);
            // 关闭资源
            EntityUtils.consume(entity);
            return response;
        }
    }

    private static void setProxyAddress() {
        System.setProperty("http.maxRedirects", "50");
        System.getProperties().setProperty("proxySet", "true");
        System.getProperties().setProperty("http.proxyHost", "163.204.245.138");
        System.getProperties().setProperty("http.proxyPort", "9999");
    }
}
