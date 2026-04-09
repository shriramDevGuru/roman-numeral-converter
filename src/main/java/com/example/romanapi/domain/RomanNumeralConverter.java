package com.example.romanapi.domain;

import java.util.List;

public class RomanNumeralConverter {
  private static final List<Entry> TABLE =
      List.of(
          new Entry(1000, "M"),
          new Entry(900, "CM"),
          new Entry(500, "D"),
          new Entry(400, "CD"),
          new Entry(100, "C"),
          new Entry(90, "XC"),
          new Entry(50, "L"),
          new Entry(40, "XL"),
          new Entry(10, "X"),
          new Entry(9, "IX"),
          new Entry(5, "V"),
          new Entry(4, "IV"),
          new Entry(1, "I"));

  public String toRoman(int value) {
    if (value < 1 || value > 3999) {
      throw new IllegalArgumentException("value out of range");
    }
    StringBuilder sb = new StringBuilder();
    int remaining = value;
    for (Entry e : TABLE) {
      while (remaining >= e.value) {
        sb.append(e.symbol);
        remaining -= e.value;
      }
      if (remaining == 0) {
        break;
      }
    }
    return sb.toString();
  }

  private record Entry(int value, String symbol) {}
}
