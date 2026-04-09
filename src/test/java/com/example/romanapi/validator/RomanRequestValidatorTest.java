package com.example.romanapi.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.romanapi.exception.InvalidRequestException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RomanRequestValidatorTest {
  private final RomanRequestValidator validator = new RomanRequestValidator();

  @Test
  void acceptsSingleMode() {
    RomanRequestValidator.RequestMode mode =
        validator.validate(Optional.of(9), Optional.empty(), Optional.empty());
    RomanRequestValidator.Single single = (RomanRequestValidator.Single) mode;
    assertEquals(9, single.value());
  }

  @Test
  void acceptsRangeMode() {
    RomanRequestValidator.RequestMode mode =
        validator.validate(Optional.empty(), Optional.of(1), Optional.of(3));
    RomanRequestValidator.Range range = (RomanRequestValidator.Range) mode;
    assertEquals(1, range.min());
    assertEquals(3, range.max());
  }

  @Test
  void rejectsMixedModeQueryAndRange() {
    assertThrows(
        InvalidRequestException.class,
        () -> validator.validate(Optional.of(1), Optional.of(1), Optional.of(2)));
  }

  @Test
  void rejectsMissingBothModes() {
    assertThrows(
        InvalidRequestException.class,
        () -> validator.validate(Optional.empty(), Optional.empty(), Optional.empty()));
  }

  @Test
  void rejectsRangeWhenOnlyOneBoundProvided() {
    assertThrows(
        InvalidRequestException.class,
        () -> validator.validate(Optional.empty(), Optional.of(1), Optional.empty()));
    assertThrows(
        InvalidRequestException.class,
        () -> validator.validate(Optional.empty(), Optional.empty(), Optional.of(2)));
  }

  @Test
  void rejectsOutOfBounds() {
    assertThrows(
        InvalidRequestException.class,
        () -> validator.validate(Optional.of(0), Optional.empty(), Optional.empty()));
    assertThrows(
        InvalidRequestException.class,
        () -> validator.validate(Optional.empty(), Optional.of(1), Optional.of(4000)));
  }

  @Test
  void rejectsMinNotLessThanMax() {
    assertThrows(
        InvalidRequestException.class,
        () -> validator.validate(Optional.empty(), Optional.of(2), Optional.of(2)));
    assertThrows(
        InvalidRequestException.class,
        () -> validator.validate(Optional.empty(), Optional.of(3), Optional.of(2)));
  }
}
