package com.example.romanapi.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

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
    return Executors.newFixedThreadPool(threads);
  }
}
