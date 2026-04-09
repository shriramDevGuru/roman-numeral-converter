package com.example.romanapi;

import org.junit.jupiter.api.Test;

class RomanApiApplicationMainTest {
  @Test
  void mainRuns() {
    // Smoke test: the Spring Boot main method can be invoked.
    // Keep the default web application type so SecurityConfig can initialize.
    RomanApiApplication.main(new String[] {});
  }
}
