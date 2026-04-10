package com.example.romanapi.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class ApiKeyAuthFilterTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void skipsPublicPaths() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "k", mapper);
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    verify(chain).doFilter(req, res);
  }

  @Test
  void skipsActuatorHealth() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "k", mapper);
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/actuator/health");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    verify(chain).doFilter(req, res);
  }

  @Test
  void skipsV3ApiDocs() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "k", mapper);
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/v3/api-docs");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    verify(chain).doFilter(req, res);
  }

  @Test
  void returns401WhenExpectedKeyIsNull() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", null, mapper);
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    req.addHeader("x-api-key", "any");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    assertEquals(401, res.getStatus());
    verifyNoInteractions(chain);
  }

  @Test
  void returns401WhenPresentedKeyDoesNotMatch() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "expected", mapper);
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    req.addHeader("x-api-key", "wrong");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    assertEquals(401, res.getStatus());
    verifyNoInteractions(chain);
  }

  @Test
  void returns401WhenPresentedKeyIsBlank() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "expected", mapper);
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    req.addHeader("x-api-key", "   ");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    assertEquals(401, res.getStatus());
    verifyNoInteractions(chain);
  }

  @Test
  void readsConfiguredHeaderName() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("custom-key", "secret", mapper);
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    req.addHeader("x-api-key", "secret");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    assertEquals(401, res.getStatus());
    verifyNoInteractions(chain);
  }

  @Test
  void returns401WhenServerKeyNotConfigured() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "", mapper);
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    assertEquals(401, res.getStatus());
    verifyNoInteractions(chain);
  }

  @Test
  void returns401WhenKeyMissingOrInvalid() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "expected", mapper);
    FilterChain chain = mock(FilterChain.class);

    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/romannumeral");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);

    assertEquals(401, res.getStatus());
    verifyNoInteractions(chain);
  }

  @Test
  void allowsRequestAndSetsAuthenticationWhenKeyValid() throws Exception {
    ApiKeyAuthFilter filter = new ApiKeyAuthFilter("x-api-key", "expected", mapper);
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
