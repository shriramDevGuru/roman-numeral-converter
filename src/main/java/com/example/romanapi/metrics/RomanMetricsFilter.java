package com.example.romanapi.metrics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

public class RomanMetricsFilter extends OncePerRequestFilter {
  private final ApiMetrics metrics;

  public RomanMetricsFilter(ApiMetrics metrics) {
    this.metrics = metrics;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();
    return path == null || !path.equals("/romannumeral");
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    var sample = metrics.startTimer();
    try {
      filterChain.doFilter(request, response);
    } finally {
      // Classify based on request intent (params) and outcome (status).
      // We intentionally keep this logic out of controllers/AOP.
      String query = request.getParameter("query");
      String min = request.getParameter("min");
      String max = request.getParameter("max");

      if (query != null) {
        metrics.incSingle();
      } else if (min != null && max != null) {
        metrics.incRange();
      }

      if (response.getStatus() == HttpStatus.BAD_REQUEST.value()) {
        metrics.incInvalid();
      }

      metrics.stopTimer(sample);
    }
  }
}

