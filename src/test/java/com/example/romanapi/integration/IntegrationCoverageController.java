package com.example.romanapi.integration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-scoped endpoints (under {@code src/test}) so integration tests can exercise exception
 * handling branches that the public API does not expose.
 */
@RestController
@RequestMapping("/__it")
public class IntegrationCoverageController {

  @GetMapping("/missing-param")
  public void missingParam(@RequestParam("required") String required) {
    // Deliberately unused: Spring throws MissingServletRequestParameterException when absent.
  }

  @GetMapping("/illegal-argument")
  public void illegalArgument() {
    throw new IllegalArgumentException("probe-iae");
  }

  @GetMapping("/unexpected")
  public void unexpected() {
    throw new RuntimeException("probe-unexpected");
  }
}
