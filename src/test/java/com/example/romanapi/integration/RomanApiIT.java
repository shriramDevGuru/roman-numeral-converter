package com.example.romanapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.romanapi.RomanApiApplication;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
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
      "app.range-executor.threads=4",
      "management.endpoints.web.exposure.include=health,metrics"
    })
@Import(IntegrationCoverageController.class)
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
  void openApiDocsArePublic() {
    ResponseEntity<String> resp = rest.getForEntity(url("/v3/api-docs"), String.class);
    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertTrue(resp.getBody().contains("openapi"));
  }

  @Test
  void swaggerUiIsPublic() {
    ResponseEntity<String> resp = rest.getForEntity(url("/swagger-ui/index.html"), String.class);
    assertTrue(resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode() == HttpStatus.FOUND);
  }

  @Test
  void romannumeralRequiresApiKey() {
    ResponseEntity<String> resp = rest.getForEntity(url("/romannumeral?query=9"), String.class);
    assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
  }

  @Test
  void romannumeralRejectsWrongApiKey() {
    HttpHeaders h = new HttpHeaders();
    h.add("x-api-key", "wrong-key");
    ResponseEntity<String> resp =
        rest.exchange(
            url("/romannumeral?query=1"), HttpMethod.GET, new HttpEntity<>(h), String.class);
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
  void romannumeralLargeSingleValue() {
    ResponseEntity<Map<String, Object>> resp =
        rest.exchange(
            url("/romannumeral?query=3333"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals("MMMCCCXXXIII", String.valueOf(resp.getBody().get("output")));
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
  void romannumeralRangeExercisesMoreValues() {
    ResponseEntity<Map<String, Object>> resp =
        rest.exchange(
            url("/romannumeral?min=1&max=40"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals(40, ((List<?>) resp.getBody().get("conversions")).size());
  }

  @Test
  void requestWithBlankClientRequestIdIsAccepted() {
    HttpHeaders headers = headersWithKey();
    headers.add("x-request-id", "   ");
    ResponseEntity<Map<String, Object>> resp =
        rest.exchange(
            url("/romannumeral?query=1"),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, resp.getStatusCode());
  }

  @Test
  void requestWithExplicitClientRequestIdIsAccepted() {
    HttpHeaders headers = headersWithKey();
    headers.add("x-request-id", "it-correlation-id");
    ResponseEntity<Map<String, Object>> resp =
        rest.exchange(
            url("/romannumeral?query=2"),
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.OK, resp.getStatusCode());
  }

  @Test
  void romannumeralReturnsBadRequestWhenQueryOutOfRange() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/romannumeral?query=4000"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertNotNull(resp.getBody().get("error"));
  }

  @Test
  void romannumeralReturnsBadRequestWhenOnlyMaxProvided() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/romannumeral?max=3"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertNotNull(resp.getBody().get("error"));
  }

  @Test
  void romannumeralReturnsBadRequestWhenQueryWithOnlyMin() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/romannumeral?query=1&min=1"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertNotNull(resp.getBody().get("error"));
  }

  @Test
  void globalHandlerMapsMissingServletParameter() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/__it/missing-param"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertEquals("missing required parameters", resp.getBody().get("error"));
  }

  @Test
  void globalHandlerMapsIllegalArgument() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/__it/illegal-argument"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertEquals("probe-iae", resp.getBody().get("error"));
  }

  @Test
  void globalHandlerMapsUnexpectedException() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/__it/unexpected"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
    assertEquals("internal server error", resp.getBody().get("error"));
  }

  @Test
  void romannumeralReturnsBadRequestWhenInvalidWithApiKey() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/romannumeral?query=0"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertNotNull(resp.getBody().get("error"));
  }

  @Test
  void romannumeralReturnsBadRequestWhenParamsMissingWithApiKey() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/romannumeral"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertNotNull(resp.getBody());
    assertNotNull(resp.getBody().get("error"));
  }

  @Test
  void romannumeralReturnsBadRequestWhenQueryMixedWithRange() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/romannumeral?query=1&min=1&max=3"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertNotNull(resp.getBody().get("error"));
  }

  @Test
  void romannumeralReturnsBadRequestWhenOnlyMinProvided() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/romannumeral?min=1"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertNotNull(resp.getBody().get("error"));
  }

  @Test
  void romannumeralReturnsBadRequestWhenMinNotLessThanMax() {
    ResponseEntity<Map<String, String>> resp =
        rest.exchange(
            url("/romannumeral?min=2&max=2"),
            HttpMethod.GET,
            new HttpEntity<>(headersWithKey()),
            new ParameterizedTypeReference<>() {});

    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    assertNotNull(resp.getBody().get("error"));
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
