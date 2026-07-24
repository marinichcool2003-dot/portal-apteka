package com.apteka.portal.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class AsyncConfig {

    private ThreadPoolTaskExecutor auditExecutorInstance;

    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        
        executor.setThreadNamePrefix("Audit-");
        executor.setKeepAliveSeconds(60);
        
        executor.setRejectedExecutionHandler((task, pool) -> {
            log.warn("Audit task rejected: active={}, queueSize={}", pool.getActiveCount(), pool.getQueue().size());
            new ThreadPoolExecutor.AbortPolicy().rejectedExecution(task, pool);
        });
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        this.auditExecutorInstance = executor;
        
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
    
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void logAuditExecutorStats() {
        if (auditExecutorInstance != null && auditExecutorInstance.getThreadPoolExecutor() != null) {
            var pool = auditExecutorInstance.getThreadPoolExecutor();
            log.info("AuditExecutor Stats - Active: {}, PoolSize: {}, QueueSize: {}, Completed: {}, TaskCount: {}",
                    pool.getActiveCount(),
                    pool.getPoolSize(),
                    pool.getQueue().size(),
                    pool.getCompletedTaskCount(),
                    pool.getTaskCount());
        }
    }
}
