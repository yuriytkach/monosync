package com.yuriytkach.monosync;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;

import com.yuriytkach.monosync.service.MonoService;
import com.yuriytkach.monosync.service.StatementsProcessor;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@QuarkusMain
@CommandLine.Command(name = "main", mixinStandardHelpOptions = true)
@SuppressWarnings("VisibilityModifier")
public class MonoSyncApp implements QuarkusApplication, Runnable {

  @Inject
  CommandLine.IFactory factory;

  @Inject
  MonoService monoService;

  @Inject
  StatementsProcessor statementsProcessor;

  @CommandLine.Option(names = { "-t", "--token" }, description = "Monobank personal token", required = true)
  String token;

  @CommandLine.Option(names = { "-d", "--days" }, description = "Days to sync (default 30)", defaultValue = "30")
  Integer daysToSync;

  @CommandLine.Option(names = { "-n", "--iban" }, description = "IBAN of account to sync")
  String iban;

  @Override
  public int run(final String... args) {
    return new CommandLine(this, factory).execute(args);
  }

  public static void main(final String... args) {
    Quarkus.run(MonoSyncApp.class, args);
  }

  @Override
  public void run() {
    log.info("Sync monobank");

    final LocalDate now = LocalDate.now();
    final LocalDate start = now.minus(daysToSync, DAYS);

    log.info("Syncing {} days. From {} to {}", daysToSync, start, now);

    monoService.loadClientInfo(token).ifPresent(client -> {
      log.info("Load data for client id: {}", client.getClientId());
      log.trace("Client name: {}", client.getName());

      client.getAccounts().stream()
        .filter(acc -> StringUtils.isBlank(iban) || iban.equals(acc.getIban()))
        .forEach(account -> statementsProcessor.processStatementsForAccount(
        account,
        token,
        start,
        now
      ));
    });
  }
}
