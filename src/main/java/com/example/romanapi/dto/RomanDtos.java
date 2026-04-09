package com.example.romanapi.dto;

import java.util.List;

public final class RomanDtos {
  private RomanDtos() {}

  public record ConversionItem(String input, String output) {}

  public record SingleResponse(String input, String output) {}

  public record RangeResponse(List<ConversionItem> conversions) {}
}

