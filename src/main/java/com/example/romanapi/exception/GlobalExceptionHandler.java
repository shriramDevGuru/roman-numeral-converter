package com.example.romanapi.exception;

import com.example.romanapi.metrics.ApiMetrics;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final ApiMetrics metrics;

  public GlobalExceptionHandler(ApiMetrics metrics) {
    this.metrics = metrics;
  }

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Map<String, String>> handleInvalidRequest(InvalidRequestException ex) {
    metrics.incInvalid();
    log.warn("validation_failed message={}", ex.getMessage());
    return json(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, String>> handleMissingParam(
      MissingServletRequestParameterException ex) {
    metrics.incInvalid();
    log.warn("validation_failed message={}", "missing required parameters");
    return json(HttpStatus.BAD_REQUEST, "missing required parameters");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
    metrics.incInvalid();
    log.warn("validation_failed message={}", ex.getMessage());
    return json(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
    log.error("unhandled_exception", ex);
    return json(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error");
  }

  private ResponseEntity<Map<String, String>> json(HttpStatus status, String message) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new ResponseEntity<>(Map.of("error", message), headers, status);
  }
}
