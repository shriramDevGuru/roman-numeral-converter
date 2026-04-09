package com.example.romanapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.api-key.value=test-key")
@AutoConfigureMockMvc
class RomanNumeralControllerSecurityTest {
  @Autowired private MockMvc mvc;

  @Test
  void rejectsMissingApiKey() throws Exception {
    mvc.perform(get("/romannumeral").queryParam("query", "1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void allowsWithValidApiKey() throws Exception {
    mvc.perform(get("/romannumeral").queryParam("query", "1").header("x-api-key", "test-key"))
        .andExpect(status().isOk());
  }
}

