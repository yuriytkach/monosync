package com.yuriytkach.monosync.model;

import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.yuriytkach.monosync.util.CurrencyMapper;

class StatementTest {

  @ParameterizedTest
  @CsvSource({
    "1601575450, 2020/10/01",
    "1600575450, 2020/09/20"
  })
  void shouldReturnPrintedDate(final long time, final String expectedDate) {
    final Statement statement = new Statement("id", time, null, 0, true, 0, 0, 0, 0, 0, 0);
    assertThat(statement.getPrintedDate()).isEqualTo(expectedDate);
  }

  @ParameterizedTest
  @CsvSource({
    "1234, 12.34",
    "-1234567, -12345.67"
  })
  void shouldReturnPrintedAmount(final long amount, final String expectedAmount) {
    final Statement statement = new Statement("id", 0, null, 0, true, amount, 0, 0, 0, 0, 0);
    assertThat(statement.getPrintedAmount()).isEqualTo(expectedAmount);
  }

  @ParameterizedTest
  @CsvSource({
    "5000, 5000, 980, 980, 4242, 2323, ' (cashback: 23.23) (commission: 42.42)'",
    "5000, 5000, 980, 980, 0, 2323, ' (cashback: 23.23)'",
    "5000, 5000, 980, 980, 4242, 0, ' (commission: 42.42)'",
    "5000, 5000, 980, 980, 0, 0, ''",
    "5000, 123, 999, 980, 4242, 2323, ' (1.23 XXX @ 40.6504) (cashback: 23.23) (commission: 42.42)'",
  })
  void shouldReturnPrintedDescription(
    final long amount,
    final long operationAmount,
    final int currencyCode,
    final int accountCurrency,
    final long commissionRate,
    final long cashbackAmount,
    final String expectedPrefix
  ) {
    var currencyMapper = mock(CurrencyMapper.class);
    when(currencyMapper.str(999)).thenReturn("XXX");

    final Instant now = Instant.now();

    final Statement statement = new Statement(
      "id",
      now.toEpochMilli() / 1000,
      "desc",
      0,
      true,
      amount,
      operationAmount,
      currencyCode,
      commissionRate,
      cashbackAmount,
      0
    );

    assertThat(statement.getPrintedDescription(currencyMapper, accountCurrency))
      .isEqualTo(
        "desc [on " + now.truncatedTo(MINUTES).atZone(systemDefault()).toLocalTime().toString() + "]" + expectedPrefix
      );
  }

}
