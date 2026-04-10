package com.example.romanapi.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class ApiMetricsTest {

  @Test
  void incrementsAndTimer() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    ApiMetrics metrics = new ApiMetrics(registry);

    metrics.incSingle();
    metrics.incRange();
    metrics.incInvalid();

    assertEquals(1.0, registry.counter("roman.single.requests").count());
    assertEquals(1.0, registry.counter("roman.range.requests").count());
    assertEquals(1.0, registry.counter("roman.invalid.requests").count());

    var sample = metrics.startTimer();
    metrics.stopTimer(sample);
    assertEquals(1, registry.timer("roman.request.latency").count());
  }
}
