package cn.zhh.crawler.util;

import cn.zhh.common.constant.SysConsts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
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
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * 请求类
 *
 * @author Zhou Huanghua
 */
public class Request {

    private String url;
    private Map<String, String> headerMap;
    private Map<String, String> queryStringParameterMap;
    private Map<String, String> formDataMap;
    private String proxyIp;
    private String proxyPort;
    @Deprecated
    private int retryCount;

    private Request() {
    }

    public static Builder builder() {
        return new Builder(new Request());
    }

    public String getByHttpClient() throws Exception {
        // 构造Get请求
        HttpGet httpGet = new HttpGet(buildUriWithParams());

        return httpClientRequest(httpGet);
    }

    public String postByHttpClient() throws Exception {
        // 构造Post请求
        HttpPost httpPost = new HttpPost(buildUriWithParams());

        // 设置表单参数
        if (!CollectionUtils.isEmpty(formDataMap)) {
            List<NameValuePair> paramList = new LinkedList();
            formDataMap.forEach((k, v) -> {
                BasicNameValuePair param = new BasicNameValuePair(k, v);
                paramList.add(param);
            });
            httpPost.setEntity(new UrlEncodedFormEntity(paramList, SysConsts.ENCODING));
        }

        return httpClientRequest(httpPost);
    }

    public String getByJsoup() throws Exception {
        setProxy();
        return buildJsoupConnection().method(Connection.Method.GET).execute().body();
    }

    public String postByJsoup() throws Exception {
        setProxy();
        return buildJsoupConnection().method(Connection.Method.POST).execute().body();
    }

    private URI buildUriWithParams() throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (!CollectionUtils.isEmpty(queryStringParameterMap)) {
            queryStringParameterMap.forEach(uriBuilder::addParameter);
        }
        return uriBuilder.build();
    }

    private String httpClientRequest(HttpRequestBase httpRequest) throws IOException {
        // 设置请求头
        if (!CollectionUtils.isEmpty(headerMap)) {
            headerMap.forEach(httpRequest::addHeader);
        }

        // 设置代理地址
        setProxy();

        //  发起请求
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

    private Connection buildJsoupConnection() throws URISyntaxException {
        Connection connection = Jsoup.connect(buildUriWithParams().toString());
        if (!CollectionUtils.isEmpty(headerMap)) {
            connection.headers(headerMap);
        }
        if (!CollectionUtils.isEmpty(formDataMap)) {
            connection.data(formDataMap);
        }
        return connection;
    }

    private void setProxy() {
        if (StringUtils.hasText(proxyIp) && Objects.nonNull(proxyPort)) {
            System.setProperty("http.maxRedirects", "50");
            System.getProperties().setProperty("proxySet", "true");
            System.getProperties().setProperty("http.proxyHost", proxyIp);
            System.getProperties().setProperty("http.proxyPort", proxyPort);
        }
    }

    public static class Builder {
        private String url;
        private Map<String, String> headerMap;
        private Map<String, String> queryStringParameterMap;
        private Map<String, String> formDataMap;
        private String proxyIp;
        private String proxyPort;
        @Deprecated
        private int retryCount;

        private Builder(Request request) {
            headerMap = new LinkedHashMap<>();
            queryStringParameterMap = new LinkedHashMap<>();
            formDataMap = new LinkedHashMap<>();
        }

        public Builder urlNonParams(String urlNonParams) {
            this.url = urlNonParams;
            return this;
        }

        public Builder addHeader(String name, String value) {
            this.headerMap.put(name, value);
            return this;
        }

        public Builder addHeaders(Map<String, String> headerMap) {
            this.headerMap.putAll(headerMap);
            return this;
        }

        public Builder addQueryStringParameter(String name, String value) {
            this.queryStringParameterMap.put(name, value);
            return this;
        }

        public Builder addQueryStringParameters(Map<String, String> queryStringParameterMap) {
            this.queryStringParameterMap.putAll(queryStringParameterMap);
            return this;
        }

        public Builder addFormData(String name, String value) {
            this.formDataMap.put(name, value);
            return this;
        }

        public Builder addFormDatas(Map<String, String> formDataMap) {
            this.formDataMap.putAll(formDataMap);
            return this;
        }

        public Builder proxy(String proxyIp, int proxyPort) {
            this.proxyIp = proxyIp;
            this.proxyPort = String.valueOf(proxyPort);
            return this;
        }

        public Builder proxy(String proxy) {
            String[] split = proxy.split(":");
            this.proxyIp = split[0];
            this.proxyPort = split[1];
            return this;
        }

        @Deprecated
        public Builder retryCount(int count) {
            this.retryCount = count;
            return this;
        }

        public Request build() {
            Request request = new Request();
            request.url = this.url;
            request.headerMap = this.headerMap;
            request.queryStringParameterMap = this.queryStringParameterMap;
            request.formDataMap = this.formDataMap;
            request.proxyIp = this.proxyIp;
            request.proxyPort = this.proxyPort;
            request.retryCount = this.retryCount;
            return request;
        }
    }
}
