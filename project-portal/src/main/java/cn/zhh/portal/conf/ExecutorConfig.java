package cn.zhh.portal.conf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线城池配置类
 *
 * @author Zhou Huanghua
 */
@Configuration
@EnableAsync
@Slf4j
public class ExecutorConfig {

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        log.info("start asyncExecutor.......");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 配置核心线程数
        executor.setCorePoolSize(8);
        // 配置最大线程数
        executor.setMaxPoolSize(16);
        // 配置队列大小
        executor.setQueueCapacity(1000);
        // 配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("async-");
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待任务在关机时完成--表明等待所有线程执行完
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间 30分钟后强制停止
        executor.setAwaitTerminationSeconds(60 * 30);
        // 执行初始化
        executor.initialize();
        return executor;
    }
}
