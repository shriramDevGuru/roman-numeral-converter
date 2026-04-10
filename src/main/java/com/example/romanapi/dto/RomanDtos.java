package com.example.romanapi.dto;

import java.util.List;

public final class RomanDtos {
  private RomanDtos() {}

  public record ConversionItem(String input, String output) {}

  public sealed interface RomanResponse permits SingleResponse, RangeResponse {}

  public record SingleResponse(String input, String output) implements RomanResponse {}

  public record RangeResponse(List<ConversionItem> conversions) implements RomanResponse {}
}
