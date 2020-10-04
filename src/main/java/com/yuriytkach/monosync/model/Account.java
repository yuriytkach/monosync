package com.yuriytkach.monosync.model;

import java.util.List;

import lombok.Data;

/**
 * <pre>
 *   {
 *       "id": "kKGVoZuHWzqVoZuH",
 *       "balance": 10000000,
 *       "creditLimit": 10000000,
 *       "currencyCode": 980,
 *       "cashbackType": "UAH",
 *       "maskedPan":["537541******0011"],
 *       "type":"black",
 *       "iban":"UA000000000000000000000"
 *     }
 * </pre>
 */
@Data
public class Account {
  private final String id;
  private final long balance;
  private final long creditLimit;
  private final int currencyCode;
  private final String cashbackType;
  private final List<String> maskedPan;
  private final String type;
  private final String iban;
}
