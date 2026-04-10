package com.example.romanapi.metrics;

import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RomanMetricsAspect {
  private final ApiMetrics metrics;

  public RomanMetricsAspect(ApiMetrics metrics) {
    this.metrics = metrics;
  }

  @Around("execution(* com.example.romanapi.controller.RomanNumeralController.convert(..))")
  public Object aroundRomanEndpoint(ProceedingJoinPoint pjp) throws Throwable {
    var sample = metrics.startTimer();
    try {
      Object[] args = pjp.getArgs();
      // Controller method signature uses Optional<Integer> for query params.
      // Aspect reads them positionally to decide which counter to increment without duplicating controller logic.
      @SuppressWarnings("unchecked")
      Optional<Integer> query = (Optional<Integer>) args[0];
      @SuppressWarnings("unchecked")
      Optional<Integer> min = (Optional<Integer>) args[1];
      @SuppressWarnings("unchecked")
      Optional<Integer> max = (Optional<Integer>) args[2];

      if (query.isPresent()) {
        metrics.incSingle();
      } else if (min.isPresent() && max.isPresent()) {
        metrics.incRange();
      }

      return pjp.proceed();
    } finally {
      metrics.stopTimer(sample);
    }
  }
}
