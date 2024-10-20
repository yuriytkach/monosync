package com.yuriytkach.monosync.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.yuriytkach.monosync.api.MonoApi;
import com.yuriytkach.monosync.model.Client;
import com.yuriytkach.monosync.model.Statement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@SuppressWarnings("VisibilityModifier")
public class MonoService {

  @Inject
  @RestClient
  MonoApi monoApi;

  public Optional<Client> loadClientInfo(final String token) {
    return Optional.ofNullable(monoApi.clientInfo(token));
  }

  public List<Statement> loadStatements(
    final String token,
    final String accountId,
    final LocalDate from,
    final LocalDate to
  ) {
    return readTransactions(token, accountId, toEpochMilli(from), toEpochMilli(to.plusDays(1)) - 1).toList();
  }

  /**
   * Повертає 500 транзакцій з кінця, тобто від часу to до from.
   * Якщо кількість транзакцій = 500, потрібно зробити ще один запит,
   * зменшивши час to до часу останнього платежу, з відповіді.
   * Якщо знову кількість транзакцій = 500, то виконуєте запити до того часу,
   * поки кількість транзакцій не буде < 500. Відповідно, якщо кількість транзакцій < 500,
   * то вже отримано всі платежі за вказаний період.
   */
  private Stream<Statement> readTransactions(
    final String token,
    final String account,
    final long startTimestamp,
    final long endTimestamp
  ) {
    final List<Statement> statements = monoApi.statements(token, account, startTimestamp, endTimestamp);
    final Stream<Statement> nextResponse;
    if (statements.size() > 500) {
      return readTransactions(token, account, startTimestamp, statements.getLast().getTime() - 1);
    } else {
      nextResponse = Stream.empty();
    }
    log.debug("Fetched mono TXes for account {}: {}", account, statements.size());
    return Stream.concat(statements.stream(), nextResponse);
  }

  private long toEpochMilli(final LocalDate from) {
    return from.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }
}
