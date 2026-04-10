package com.example.romanapi.exception;

import com.example.romanapi.metrics.ApiMetrics;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  public GlobalExceptionHandler(ApiMetrics metrics) {}

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Map<String, String>> handleInvalidRequest(InvalidRequestException ex) {
    log.warn("validation_failed message={}", ex.getMessage());
    return json(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, String>> handleMissingParam(
      MissingServletRequestParameterException ex) {
    log.warn("validation_failed message={}", "missing required parameters");
    return json(HttpStatus.BAD_REQUEST, "missing required parameters");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("validation_failed message={}", ex.getMessage());
    return json(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
    log.error("unhandled_exception", ex);
    return json(HttpStatus.INTERNAL_SERVER_ERROR, "internal server error");
  }

  private ResponseEntity<Map<String, String>> json(@NonNull HttpStatusCode status, String message) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return ResponseEntity.status(status).headers(headers).body(Map.of("error", message));
  }
}
