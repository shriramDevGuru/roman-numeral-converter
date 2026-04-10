package com.example.romanapi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class AsyncConfigTest {

  @Test
  void usesDefaultPoolWhenThreadsNotSet() {
    MockEnvironment env = new MockEnvironment();

    AsyncConfig cfg = new AsyncConfig(env);
    Executor ex = cfg.rangeExecutor();

    ThreadPoolExecutor tpe = (ThreadPoolExecutor) ex;
    assertEquals(true, tpe.getCorePoolSize() >= 2);
    tpe.shutdownNow();
  }

  @Test
  void usesConfiguredThreadCountWhenProvided() {
    MockEnvironment env = new MockEnvironment();
    env.setProperty("app.range-executor.threads", "5");

    AsyncConfig cfg = new AsyncConfig(env);
    Executor ex = cfg.rangeExecutor();

    ThreadPoolExecutor tpe = (ThreadPoolExecutor) ex;
    assertEquals(5, tpe.getCorePoolSize());
    tpe.shutdownNow();
  }
}
