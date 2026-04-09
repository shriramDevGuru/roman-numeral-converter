package com.example.romanapi.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class ApiMetrics {
  private final Counter singleRequests;
  private final Counter rangeRequests;
  private final Counter invalidRequests;
  private final Timer requestLatency;

  public ApiMetrics(MeterRegistry registry) {
    this.singleRequests = registry.counter("roman.single.requests");
    this.rangeRequests = registry.counter("roman.range.requests");
    this.invalidRequests = registry.counter("roman.invalid.requests");
    this.requestLatency = registry.timer("roman.request.latency");
  }

  public void incSingle() {
    singleRequests.increment();
  }

  public void incRange() {
    rangeRequests.increment();
  }

  public void incInvalid() {
    invalidRequests.increment();
  }

  public Timer.Sample startTimer() {
    return Timer.start();
  }

  public void stopTimer(Timer.Sample sample) {
    sample.stop(requestLatency);
  }
}

