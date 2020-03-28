package cn.zhh.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * 前台模块启动类
 *
 * @author Zhou Huanghua
 */
@SpringBootApplication
@ServletComponentScan({"cn.zhh.portal.filter"})
public class PortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}
