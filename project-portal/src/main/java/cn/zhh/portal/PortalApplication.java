package cn.zhh.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 前台模块启动类
 *
 * @author Zhou Huanghua
 */
@SpringBootApplication
public class PortalApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(PortalApplication.class);
    }
}
