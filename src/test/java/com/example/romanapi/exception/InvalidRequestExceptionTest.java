package com.example.romanapi.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InvalidRequestExceptionTest {

  @Test
  void carriesMessage() {
    InvalidRequestException ex = new InvalidRequestException("oops");
    assertEquals("oops", ex.getMessage());
  }
}
