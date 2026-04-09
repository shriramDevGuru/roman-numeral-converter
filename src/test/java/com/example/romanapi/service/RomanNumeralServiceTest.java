package com.example.romanapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.romanapi.dto.RomanDtos.RangeResponse;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;

class RomanNumeralServiceTest {

  @Test
  void rangeConversionReturnsAscendingOrder() {
    Executor sameThread = Runnable::run;
    RomanNumeralService service = new RomanNumeralService(sameThread);

    RangeResponse res = service.convertRange(1, 5);
    assertEquals(5, res.conversions().size());
    assertEquals("1", res.conversions().get(0).input());
    assertEquals("I", res.conversions().get(0).output());
    assertEquals("5", res.conversions().get(4).input());
    assertEquals("V", res.conversions().get(4).output());
  }
}

