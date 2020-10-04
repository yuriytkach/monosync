package com.yuriytkach.monosync.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CurrencyMapperTest {

  @Test
  void shouldConvertToMap() {
    final CurrencyMapper currencyMapper = new CurrencyMapper();
    currencyMapper.currencies = List.of("111/USD", "222/UAH");
    currencyMapper.convertToMap();

    assertThat(currencyMapper.codes).containsOnly(
      entry(111, "USD"),
      entry(222, "UAH")
    );
  }

  @ParameterizedTest
  @CsvSource({
    "111, UAH",
    "222, `222`"
  })
  void shouldReturnStringForCode(final int code, final String expected) {
    final CurrencyMapper currencyMapper = new CurrencyMapper();
    currencyMapper.codes = Map.of(111, "UAH");

    assertThat(currencyMapper.str(code)).isEqualTo(expected);
  }
}
