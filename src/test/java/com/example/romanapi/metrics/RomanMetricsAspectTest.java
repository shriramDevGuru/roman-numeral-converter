package com.example.romanapi.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

class RomanMetricsAspectTest {

  @Test
  void incrementsSingleOnQuery() throws Throwable {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    ApiMetrics metrics = new ApiMetrics(registry);
    RomanMetricsAspect aspect = new RomanMetricsAspect(metrics);

    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    when(pjp.getArgs()).thenReturn(new Object[] {Optional.of(1), Optional.empty(), Optional.empty()});
    when(pjp.proceed()).thenReturn("ok");

    aspect.aroundRomanEndpoint(pjp);

    assertEquals(1.0, registry.counter("roman.single.requests").count());
    assertEquals(0.0, registry.counter("roman.range.requests").count());
    assertEquals(1, registry.timer("roman.request.latency").count());
  }

  @Test
  void incrementsRangeOnMinMax() throws Throwable {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    ApiMetrics metrics = new ApiMetrics(registry);
    RomanMetricsAspect aspect = new RomanMetricsAspect(metrics);

    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    when(pjp.getArgs()).thenReturn(new Object[] {Optional.empty(), Optional.of(1), Optional.of(2)});
    when(pjp.proceed()).thenReturn("ok");

    aspect.aroundRomanEndpoint(pjp);

    assertEquals(0.0, registry.counter("roman.single.requests").count());
    assertEquals(1.0, registry.counter("roman.range.requests").count());
    assertEquals(1, registry.timer("roman.request.latency").count());
  }

  @Test
  void stillStopsTimerWhenProceedThrows() throws Throwable {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    ApiMetrics metrics = new ApiMetrics(registry);
    RomanMetricsAspect aspect = new RomanMetricsAspect(metrics);

    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    when(pjp.getArgs()).thenReturn(new Object[] {Optional.empty(), Optional.empty(), Optional.empty()});
    when(pjp.proceed()).thenThrow(new RuntimeException("boom"));

    assertThrows(RuntimeException.class, () -> aspect.aroundRomanEndpoint(pjp));
    assertEquals(1, registry.timer("roman.request.latency").count());
    assertEquals(0.0, registry.counter("roman.single.requests").count());
    assertEquals(0.0, registry.counter("roman.range.requests").count());
  }
}

