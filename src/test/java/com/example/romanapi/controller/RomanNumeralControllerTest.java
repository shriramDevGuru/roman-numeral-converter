package com.example.romanapi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.api-key.value=test-key")
@AutoConfigureMockMvc
class RomanNumeralControllerTest {
  @Autowired private MockMvc mvc;

  @Test
  void singleConversionWorks() throws Exception {
    mvc.perform(get("/romannumeral").queryParam("query", "9").header("x-api-key", "test-key"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.input").value("9"))
        .andExpect(jsonPath("$.output").value("IX"));
  }

  @Test
  void rangeConversionIsSortedAscending() throws Exception {
    mvc.perform(
            get("/romannumeral")
                .queryParam("min", "1")
                .queryParam("max", "3")
                .header("x-api-key", "test-key"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.conversions.length()").value(3))
        .andExpect(jsonPath("$.conversions[0].input").value("1"))
        .andExpect(jsonPath("$.conversions[0].output").value("I"))
        .andExpect(jsonPath("$.conversions[2].input").value("3"))
        .andExpect(jsonPath("$.conversions[2].output").value("III"));
  }

  @Test
  void invalidQueryReturnsJsonError() throws Exception {
    mvc.perform(get("/romannumeral").queryParam("query", "0").header("x-api-key", "test-key"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  void missingParamsReturnsJsonError() throws Exception {
    mvc.perform(get("/romannumeral").header("x-api-key", "test-key"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists());
  }
}

