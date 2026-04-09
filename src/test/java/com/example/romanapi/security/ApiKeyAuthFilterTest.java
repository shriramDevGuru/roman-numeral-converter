package com.example.romanapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class ApiKeyAuthFilterTest {

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void skipsPublicPaths() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "k");
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    verify(chain).doFilter(req, res);
  }

  @Test
  void returns401WhenServerKeyNotConfigured() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "");
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    assertEquals(401, res.getStatus());
    verifyNoInteractions(chain);
  }

  @Test
  void returns401WhenKeyMissingOrInvalid() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "expected");
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    assertEquals(401, res.getStatus());
    verifyNoInteractions(chain);
  }

  @Test
  void allowsRequestAndSetsAuthenticationWhenKeyValid() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "expected");
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    req.addHeader("x-api-key", "expected");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    verify(chain).doFilter(req, res);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals("api-key", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }
}
