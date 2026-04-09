package com.example.romanapi.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

  @Test
  void usesIncomingRequestIdAndCleansUpMdc() throws Exception {
    RequestLoggingFilter filter = new RequestLoggingFilter();

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    req.addHeader("x-request-id", "rid-123");
    MockHttpServletResponse res = new MockHttpServletResponse();

    FilterChain chain =
        (ServletRequest request, ServletResponse response) -> {
          assertEquals("rid-123", MDC.get("requestId"));
        };

    filter.doFilter(req, res, chain);
    assertNull(MDC.get("requestId"));
  }

  @Test
  void generatesRequestIdWhenMissing() throws Exception {
    RequestLoggingFilter filter = new RequestLoggingFilter();

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    MockHttpServletResponse res = new MockHttpServletResponse();

    FilterChain chain =
        (ServletRequest request, ServletResponse response) -> {
          String id = MDC.get("requestId");
          // non-empty and cleaned up later
          assertEquals(false, id == null || id.isBlank());
        };

    filter.doFilter(req, res, chain);
    assertNull(MDC.get("requestId"));
  }
}

