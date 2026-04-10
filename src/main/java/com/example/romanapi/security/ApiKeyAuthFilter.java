package com.example.romanapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

public class ApiKeyAuthFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
  private final String headerName;
  private final String expectedValue;
  private final AntPathMatcher matcher = new AntPathMatcher();
  private final ObjectMapper mapper;

  public ApiKeyAuthFilter(String headerName, String expectedValue, ObjectMapper mapper) {
    this.headerName = headerName;
    this.expectedValue = expectedValue;
    this.mapper = mapper;
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String path = request.getRequestURI();
    if (path == null) {
      path = "";
    }
    // Keep docs and health endpoints publicly accessible so the service is easy to validate
    // and explore even before API key distribution.
    return matcher.match("/actuator/health", path)
        || matcher.match("/swagger-ui/**", path)
        || matcher.match("/v3/api-docs/**", path);
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    if (expectedValue == null || expectedValue.isBlank()) {
      // Misconfiguration should be obvious: protected endpoints are effectively down until APP_API_KEY is set.
      log.error("auth_failed reason=missing_api_key_config");
      reject(response, "server missing API key configuration");
      return;
    }

    String presented = request.getHeader(headerName);
    if (presented == null || presented.isBlank() || !matches(presented, expectedValue)) {
      log.warn("auth_failed reason=invalid_api_key path={}", request.getRequestURI());
      reject(response, "unauthorized");
      return;
    }

    var auth =
        new UsernamePasswordAuthenticationToken(
            "api-key", null, List.of(new SimpleGrantedAuthority("ROLE_API")));
    SecurityContextHolder.getContext().setAuthentication(auth);
    filterChain.doFilter(request, response);
  }

  private void reject(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    mapper.writeValue(response.getWriter(), Map.of("error", message));
  }

  private static boolean matches(String presented, String expected) {
    byte[] a = presented.getBytes(StandardCharsets.UTF_8);
    byte[] b = expected.getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(a, b);
  }
}
