package com.yuriytkach.monosync.model;

import static java.lang.String.format;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.yuriytkach.monosync.util.CurrencyMapper;

import lombok.Data;

/**
 * <pre>
 *   {
 *     "id": "ZuHWzqkKGVo=",
 *     "time": 1554466347,
 *     "description": "Покупка щастя",
 *     "mcc": 7997,
 *     "hold": false,
 *     "amount": -95000,
 *     "operationAmount": -95000,
 *     "currencyCode": 980,
 *     "commissionRate": 0,
 *     "cashbackAmount": 19000,
 *     "balance": 10050000
 *   }
 * </pre>
 */
@Data
public class Statement {
  private static final DecimalFormatSymbols FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance();
  private static final NumberFormat DECIMAL_FORMAT;
  private static final NumberFormat EXCHANGE_RATE_FORMAT;
  static {
    FORMAT_SYMBOLS.setDecimalSeparator('.');
    FORMAT_SYMBOLS.setMinusSign('-');

    DECIMAL_FORMAT = new DecimalFormat("#.##", FORMAT_SYMBOLS);
    EXCHANGE_RATE_FORMAT = new DecimalFormat("#.####", FORMAT_SYMBOLS);
  }

  private final String id;
  private final long time;
  private final String description;
  private final int mcc;
  private final boolean hold;
  private final long amount;
  private final long operationAmount;
  private final int currencyCode;
  private final long commissionRate;
  private final long cashbackAmount;
  private final long balance;

  public String getPrintedAmount() {
    return DECIMAL_FORMAT.format((double) amount / 100);
  }

  public String getPrintedDate() {
    return Instant.ofEpochSecond(time).atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
  }

  public String getPrintedDescription(final CurrencyMapper currencyMapper, final int defaultCurrency) {
    var sb = new StringBuilder(description);

    sb.append(" [on ")
      .append(Instant.ofEpochSecond(time).truncatedTo(MINUTES).atZone(systemDefault()).toLocalTime().toString())
      .append("]");

    if (defaultCurrency != currencyCode) {
      sb.append(format(
        " (%d %s @ %s",
        operationAmount,
        currencyMapper.str(currencyCode),
        EXCHANGE_RATE_FORMAT.format((double) amount / (double) operationAmount)
      )).append(")");
    }

    if (cashbackAmount > 0) {
      sb.append(" (cashback: ").append(DECIMAL_FORMAT.format((double) cashbackAmount / 100)).append(")");
    }

    if (commissionRate > 0) {
      sb.append(" (commission: ").append(DECIMAL_FORMAT.format((double) commissionRate / 100)).append(")");
    }

    return sb.toString();
  }
}
