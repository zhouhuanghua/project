package cn.zhh.crawler.service;

import cn.zhh.common.dto.mq.SearchPositionInfoMsg;
import cn.zhh.common.enums.ErrorEnum;
import cn.zhh.common.util.BusinessException;
import cn.zhh.crawler.constant.CrawlerConsts;
import cn.zhh.crawler.util.HttpClientUtils;
import cn.zhh.crawler.util.MapUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 拉勾爬虫服务
 *
 * @author Zhou Huanghua
 */
@Component
@Slf4j
public class LagouCrawlService implements CrawlService {

    @Autowired
    private MqProducer mqProducer;

    @Override
    public void crawl(SearchPositionInfoMsg searchCondition) throws Exception {
        // 执行搜索
        String url = buildUrlWithParams(searchCondition);
        Map<String, String> dataMap = MapUtils.buildMap("kd", searchCondition.getContent(), "pn", "1");
        Map<String, String> headerMap = CrawlerConsts.HEADER_MAP;
        headerMap.put("Cookie", getCookie());
        log.info("开始搜索职位，url：{}，请求参数：{}，请求头：{}", url, dataMap, headerMap);
        String rspStr = HttpClientUtils.post(CrawlerConsts.LAGOU_SEARCH_URL, dataMap, headerMap);
        JSONObject response = JSONObject.parseObject(rspStr);
        /*if (!Objects.equals(response.getInteger("code"), 0)) {
            throw new BusinessException(ErrorEnum.BAD_REQUEST, "请求响应失败！");
        }*/

        System.out.println(response);
    }

    private String getCookie() {
        CookieStore cookieStore = new BasicCookieStore();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()) {
            HttpPost httpPost = new HttpPost("https://www.lagou.com/jobs/list_运维?city=%E6%88%90%E9%83%BD&cl=false&fromSearch=true&labelWords=&suginput=");
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.87 Safari/537.36");
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        } catch (Exception e) {
            throw new BusinessException(ErrorEnum.BAD_REQUEST, e);
        }

        List<Cookie> cookieList = cookieStore.getCookies();
        if (CollectionUtils.isEmpty(cookieList)) {
            return "";
        }

        return cookieList.stream()
            .map(cookie -> cookie.getName() + "=" + cookie.getValue())
            .reduce((s1, s2) -> s1 + "; " + s2)
            .get();
    }

    private String buildUrlWithParams(SearchPositionInfoMsg searchCondition) {
        String baseUrl = CrawlerConsts.LAGOU_SEARCH_URL;
        if (StringUtils.hasText(searchCondition.getCity())) {
            // todo
        }

        return baseUrl;
    }
}
