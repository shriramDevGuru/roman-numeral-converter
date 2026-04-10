package com.example.romanapi.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
  private final Environment env;

  public AsyncConfig(Environment env) {
    this.env = env;
  }

  @Bean(name = "rangeExecutor")
  public Executor rangeExecutor() {
    int configured = env.getProperty("app.range-executor.threads", Integer.class, 0);
    // Default strategy: if not explicitly configured, pick a small CPU-based pool (minimum 2)
    // to improve range throughput while avoiding oversubscription on small machines/containers.
    int threads =
        configured > 0 ? configured : Math.max(2, Runtime.getRuntime().availableProcessors());
    ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
    exec.setCorePoolSize(threads);
    exec.setMaxPoolSize(threads);
    exec.setQueueCapacity(env.getProperty("app.range-executor.queue-capacity", Integer.class, 5000));
    exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    exec.setThreadNamePrefix("range-exec-");
    exec.setWaitForTasksToCompleteOnShutdown(true);
    exec.setAwaitTerminationSeconds(10);
    exec.initialize();
    return exec;
  }
}
