package com.yuriytkach.monosync.util;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import one.util.streamex.StreamEx;

@Dependent
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
