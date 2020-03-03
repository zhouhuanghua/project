package cn.zhh.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 爬虫模块启动类
 *
 * @author Zhou Huanghua
 */
@SpringBootApplication
public class CrawlerApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }
}
