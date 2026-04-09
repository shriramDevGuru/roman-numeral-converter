package com.example.romanapi.service;

import com.example.romanapi.domain.RomanNumeralConverter;
import com.example.romanapi.dto.RomanDtos.ConversionItem;
import com.example.romanapi.dto.RomanDtos.RangeResponse;
import com.example.romanapi.dto.RomanDtos.SingleResponse;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RomanNumeralService {
  private final RomanNumeralConverter converter = new RomanNumeralConverter();
  private final Executor rangeExecutor;

  public RomanNumeralService(@Qualifier("rangeExecutor") Executor rangeExecutor) {
    this.rangeExecutor = rangeExecutor;
  }

  public SingleResponse convertSingle(int value) {
    return new SingleResponse(String.valueOf(value), converter.toRoman(value));
  }

  public RangeResponse convertRange(int min, int max) {
    List<CompletableFuture<ConversionItem>> futures =
        java.util.stream.IntStream.rangeClosed(min, max)
            .mapToObj(
                i ->
                    CompletableFuture.supplyAsync(
                        () -> new ConversionItem(String.valueOf(i), converter.toRoman(i)), rangeExecutor))
            .toList();

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    List<ConversionItem> items =
        futures.stream()
            .map(CompletableFuture::join)
            .sorted(Comparator.comparingInt(i -> Integer.parseInt(i.input())))
            .toList();

    return new RangeResponse(items);
  }
}

