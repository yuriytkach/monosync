package com.yuriytkach.monosync.model;

import java.util.List;

import lombok.Data;

/**
 * <pre>
 *   {
 *   "clientId":"XXXXXXXXX"
 *   "name": "string",
 *   "webHookUrl": "string",
 *   "accounts": [
 *     {
 *       "id": "kKGVoZuHWzqVoZuH",
 *       "balance": 10000000,
 *       "creditLimit": 10000000,
 *       "currencyCode": 980,
 *       "cashbackType": "UAH"
 *     }
 *   ]
 * }
 * </pre>
 */
@Data
public class Client {

  private final String clientId;
  private final String name;
  private final String webHookUrl;
  private final List<Account> accounts;
}
