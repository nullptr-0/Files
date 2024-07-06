package com.nullptr.files.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${async.executor.core-pool-size:2}")
    private int corePoolSize;

    @Value("${async.executor.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${async.executor.queue-capacity:500}")
    private int queueCapacity;

    @Bean(name = "fileOperationTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("FileOperation-");
        executor.initialize();
        return executor;
    }
}
