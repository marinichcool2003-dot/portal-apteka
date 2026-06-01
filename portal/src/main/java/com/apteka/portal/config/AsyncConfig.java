package com.apteka.portal.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        
        executor.setThreadNamePrefix("Audit-");
        executor.setKeepAliveSeconds(60);
        
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        logExecutorStats(executor);
        
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
    
    private void logExecutorStats(ThreadPoolTaskExecutor executor) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (executor.getThreadPoolExecutor() != null) {
                var pool = executor.getThreadPoolExecutor();
                log.info("AuditExecutor Stats - Active: {}, PoolSize: {}, QueueSize: {}, Completed: {}, TaskCount: {}",
                        pool.getActiveCount(),
                        pool.getPoolSize(),
                        pool.getQueue().size(),
                        pool.getCompletedTaskCount(),
                        pool.getTaskCount());
            }
        }, 60, 60, TimeUnit.SECONDS);
    }
}
