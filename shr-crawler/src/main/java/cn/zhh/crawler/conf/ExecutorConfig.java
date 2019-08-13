package cn.zhh.crawler.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 线城池配置类
 *
 * @author Zhou Huanghua
 */
@Configuration
@Slf4j
public class ExecutorConfig {

    @Bean("asyncServiceExecutor")
    public Executor asyncServiceExecutor() {
        log.info("start asyncServiceExecutor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 配置核心线程数
        executor.setCorePoolSize(5);
        // 配置最大线程数
        executor.setMaxPoolSize(5);
        // 配置队列大小
        executor.setQueueCapacity(10_0000);
        // 配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("async-service-");;
        // 执行初始化
        executor.initialize();

        return executor;
    }
}
