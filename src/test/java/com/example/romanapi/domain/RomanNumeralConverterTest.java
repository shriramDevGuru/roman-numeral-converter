package com.example.romanapi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RomanNumeralConverterTest {
  private final RomanNumeralConverter converter = new RomanNumeralConverter();

  @Test
  void convertsEdgeCases() {
    assertEquals("I", converter.toRoman(1));
    assertEquals("IV", converter.toRoman(4));
    assertEquals("IX", converter.toRoman(9));
    assertEquals("XLIV", converter.toRoman(44));
    assertEquals("XCIX", converter.toRoman(99));
    assertEquals("CD", converter.toRoman(400));
    assertEquals("CMXLIV", converter.toRoman(944));
    assertEquals("MMMCMXCIX", converter.toRoman(3999));
  }

  @Test
  void rejectsOutOfRange() {
    assertThrows(IllegalArgumentException.class, () -> converter.toRoman(0));
    assertThrows(IllegalArgumentException.class, () -> converter.toRoman(4000));
  }
}

