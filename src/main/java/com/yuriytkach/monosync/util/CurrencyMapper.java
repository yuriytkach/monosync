package com.yuriytkach.monosync.util;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import one.util.streamex.StreamEx;

@Dependent
@SuppressWarnings("VisibilityModifier")
public class CurrencyMapper {

  @ConfigProperty(name = "app.currencies")
  List<String> currencies;

  Map<Integer, String> codes;

  @PostConstruct
  void convertToMap() {
    codes = StreamEx.of(currencies)
      .map(str -> str.split("/"))
      .mapToEntry(arr -> arr[0], arr -> arr[1])
      .mapKeys(Integer::valueOf)
      .toMap();
  }

  public String str(final int currencyCode) {
    return codes.getOrDefault(currencyCode, "`" + currencyCode + "`");
  }

}
