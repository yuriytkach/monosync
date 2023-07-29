package com.yuriytkach.monosync.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.yuriytkach.monosync.api.MonoApi;
import com.yuriytkach.monosync.model.ApiError;
import com.yuriytkach.monosync.model.Client;
import com.yuriytkach.monosync.model.Statement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Dependent
@SuppressWarnings("VisibilityModifier")
public class MonoService {

  @Inject
  @RestClient
  MonoApi monoApi;

  @ConfigProperty(name = "app.sleep-duration", defaultValue = "10s")
  Duration sleepDuration;

  @ConfigProperty(name = "app.max-retries", defaultValue = "5")
  int maxRetries;

  public Optional<Client> loadClientInfo(final String token) {
    return execWithRetry(() -> monoApi.clientInfo(token), maxRetries);
  }

  public Optional<List<Statement>> loadStatements(
    final String token,
    final String accountId,
    final LocalDate from,
    final LocalDate to
  ) {
    return execWithRetry(() -> monoApi.statements(token, accountId, toEpochMilli(from), toEpochMilli(to)), maxRetries);
  }

  private long toEpochMilli(final LocalDate from) {
    return from.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

  private <R> Optional<R> execWithRetry(final Supplier<R> function, final int retryCount) {
    var attempt = 0;
    while (attempt <= retryCount) {
      try {
        return Optional.ofNullable(function.get());
      } catch (final WebApplicationException ex) {
        final Response response = ex.getResponse();
        final ApiError apiError = response.readEntity(ApiError.class);

        if (response.getStatus() == 429 && attempt < retryCount) {
          log.debug(
            "Received [{}] {}. Retry attempt {} of {}",
            response.getStatus(),
            apiError.getErrorDescription(),
            ++attempt,
            retryCount
          );
          sleep(attempt);
          continue;
        }

        log.error("Failed load client info: [{}] {}", response.getStatus(), apiError.getErrorDescription());
        return Optional.empty();
      }
    }

    log.error("Exhausted retry attempts");
    return Optional.empty();
  }

  private void sleep(final int backoff) {
    try {
      final Duration toSleep = sleepDuration.multipliedBy(CombinatoricsUtils.factorial(backoff));
      log.debug("Sleeping {} sec to prevent too many requests error", toSleep.toSeconds());
      Thread.sleep(toSleep.toMillis());
    } catch (final InterruptedException ex) {
      log.error("Interrupted sleep: {}", ex.getMessage());
    }
  }
}
