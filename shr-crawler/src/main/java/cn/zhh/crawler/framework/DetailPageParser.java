package cn.zhh.crawler.framework;

import org.jsoup.nodes.Document;

/**
 * 详情页面解析器
 *
 * @author Zhou Huanghua
 */
public interface DetailPageParser<T> {

    DetailPageParser<T> newInstance();

    String parseUrl(String baseUrl, Document itemDocument);

    boolean isNormalPage(Document detailDocument);

    T generateObj(String url, Document detailDocument);

    void processObj(T obj);
}
