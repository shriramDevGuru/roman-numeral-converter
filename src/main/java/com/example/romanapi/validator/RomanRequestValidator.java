package com.example.romanapi.validator;

import com.example.romanapi.exception.InvalidRequestException;
import java.util.Optional;

public class RomanRequestValidator {

  public sealed interface RequestMode permits Single, Range {}

  public record Single(int value) implements RequestMode {}

  public record Range(int min, int max) implements RequestMode {}

  public RequestMode validate(Optional<Integer> query, Optional<Integer> min, Optional<Integer> max) {
    boolean hasQuery = query.isPresent();
    boolean hasMin = min.isPresent();
    boolean hasMax = max.isPresent();

    if (hasQuery == (hasMin || hasMax)) {
      throw new InvalidRequestException("use either query or min and max");
    }

    if (hasQuery) {
      int q = query.get();
      validateRangeValue(q, "query");
      return new Single(q);
    }

    if (!hasMin || !hasMax) {
      throw new InvalidRequestException("min and max must be provided together");
    }

    int minVal = min.get();
    int maxVal = max.get();
    validateRangeValue(minVal, "min");
    validateRangeValue(maxVal, "max");
    if (minVal >= maxVal) {
      throw new InvalidRequestException("min must be less than max");
    }
    return new Range(minVal, maxVal);
  }

  private void validateRangeValue(int v, String field) {
    if (v < 1 || v > 3999) {
      throw new InvalidRequestException(field + " must be between 1 and 3999");
    }
  }
}

