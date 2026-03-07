package com.ITQ.document_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        //2 workers - 2 threads
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("document-worker-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    @ConfigurationProperties(prefix = "app")
    public WorkerProperties workerProperties() {
        return new WorkerProperties();
    }

    @Getter
    @Setter
    public static class WorkerProperties {
        private int batchSize;
        private long workerStartDelay;
        private long submitWorkerDelay;
        private long approveWorkerDelay;

    }
}
