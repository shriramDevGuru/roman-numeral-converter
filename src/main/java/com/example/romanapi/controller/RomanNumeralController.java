package com.example.romanapi.controller;

import com.example.romanapi.dto.RomanDtos.RangeResponse;
import com.example.romanapi.dto.RomanDtos.SingleResponse;
import com.example.romanapi.service.RomanNumeralService;
import com.example.romanapi.validator.RomanRequestValidator;
import com.example.romanapi.validator.RomanRequestValidator.Range;
import com.example.romanapi.validator.RomanRequestValidator.Single;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RomanNumeralController {
  private final RomanNumeralService service;
  private final RomanRequestValidator validator = new RomanRequestValidator();

  public RomanNumeralController(RomanNumeralService service) {
    this.service = service;
  }

  @GetMapping("/romannumeral")
  public ResponseEntity<?> convert(
      @RequestParam(name = "query") Optional<Integer> query,
      @RequestParam(name = "min") Optional<Integer> min,
      @RequestParam(name = "max") Optional<Integer> max) {
    RomanRequestValidator.RequestMode mode = validator.validate(query, min, max);
    if (mode instanceof Single single) {
      SingleResponse body = service.convertSingle(single.value());
      return ResponseEntity.ok(body);
    }
    Range range = (Range) mode;
    RangeResponse body = service.convertRange(range.min(), range.max());
    return ResponseEntity.ok(body);
  }
}
