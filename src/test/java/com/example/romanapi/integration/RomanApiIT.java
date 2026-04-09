package com.example.romanapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.romanapi.RomanApiApplication;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    classes = RomanApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "app.api-key.value=test-key",
      "app.api-key.header=x-api-key",
      "management.endpoints.web.exposure.include=health,metrics"
    })
class RomanApiIT {
  @LocalServerPort private int port;

  @Autowired private TestRestTemplate rest;

  @Test
  void healthIsPublic() {
    ResponseEntity<Map<String, Object>> resp =
        rest.exchange(
            url("/actuator/health"),
            HttpMethod.GET,
            HttpEntity.EMPTY,
            new ParameterizedTypeReference<>() {});
    assertEquals(HttpStatus.OK, resp.getStatusCode());
  }

  @Test
  void romannumeralRequiresApiKey() {
    ResponseEntity<String> resp = rest.getForEntity(url("/romannumeral?query=9"), String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
  }

  @Test
  void romannumeralSingleWorksWithApiKey() {
    ResponseEntity<Map<String, Object>> resp =
        rest.exchange(
            url("/romannumeral?query=9"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals("9", String.valueOf(resp.getBody().get("input")));
    assertEquals("IX", String.valueOf(resp.getBody().get("output")));
  }

  @Test
  void romannumeralRangeWorksWithApiKey() {
    ResponseEntity<Map<String, Object>> resp =
        rest.exchange(
            url("/romannumeral?min=1&max=3"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody().get("conversions"));
  }

  @Test
  void metricsAreProtected() {
    ResponseEntity<String> resp = rest.getForEntity(url("/actuator/metrics"), String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());

    ResponseEntity<Map<String, Object>> ok =
        rest.exchange(
            url("/actuator/metrics"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});
    assertEquals(HttpStatus.OK, ok.getStatusCode());
  }

  private String url(String path) {
    return "http://localhost:" + port + path;
  }

  private HttpHeaders headersWithKey() {
    HttpHeaders h = new HttpHeaders();
    h.add("x-api-key", "test-key");
    return h;
  }
}
