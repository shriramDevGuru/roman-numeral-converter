package com.example.romanapi.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.romanapi.metrics.ApiMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;

class GlobalExceptionHandlerTest {

  @Test
  void returnsJsonForInvalidRequest() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    GlobalExceptionHandler handler = new GlobalExceptionHandler(new ApiMetrics(registry));

    var resp = handler.handleInvalidRequest(new InvalidRequestException("bad"));
    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertEquals("bad", resp.getBody().get("error"));
  }

  @Test
  void returnsJsonForMissingParam() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    GlobalExceptionHandler handler = new GlobalExceptionHandler(new ApiMetrics(registry));

    var ex = new MissingServletRequestParameterException("query", "Integer");
    var resp = handler.handleMissingParam(ex);
    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertEquals("missing required parameters", resp.getBody().get("error"));
  }

  @Test
  void returnsJsonForUnexpected() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    GlobalExceptionHandler handler = new GlobalExceptionHandler(new ApiMetrics(registry));

    var resp = handler.handleUnexpected(new RuntimeException("boom"));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    assertEquals("internal server error", resp.getBody().get("error"));
  }
}
