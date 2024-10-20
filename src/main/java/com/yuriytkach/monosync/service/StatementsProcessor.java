package com.yuriytkach.monosync.service;

import static java.time.temporal.ChronoUnit.DAYS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.yuriytkach.monosync.model.Account;
import com.yuriytkach.monosync.model.Statement;
import com.yuriytkach.monosync.util.StatementsXlsProducer;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class StatementsProcessor {

  private final MonoService monoService;
  private final StatementsXlsProducer xlsProducer;

  public void processStatementsForAccount(
    final Account account,
    final String token,
    final LocalDate from,
    final LocalDate to
  ) {
    final String balance = String.format("%.2f", (double) (account.getBalance() - account.getCreditLimit()) / 100);
    final String credit = String.format("%.2f", (double) account.getCreditLimit() / 100);
    log.info("Account {} balance: {} (credit limit: {})", account.getIban(), balance, credit);

    final List<LocalDate> dates = Stream.concat(
      from.datesUntil(to, Period.ofDays(30)),
      Stream.of(to)
    ).toList();

    final List<Statement> statements = IntStream.range(0, dates.size() - 1)
      .mapToObj(i -> {
        final var start = dates.get(i);
        final var end = i == dates.size() - 2 ? to : dates.get(i + 1).minus(1, DAYS);
        log.debug("Load statements for account {} from {} to {}", account.getIban(), start, end);
        return Map.entry(start, end);
      })
      .map(range -> monoService.loadStatements(token, account.getId(), range.getKey(), range.getValue()))
      .flatMap(Collection::stream)
      .sorted(Comparator.comparingLong(Statement::getTime))
      .toList();

    log.info("Account {} loaded statements: {}", account.getIban(), statements.size());

    if (!statements.isEmpty()) {
      final File file = new File("/tmp/statements-" + account.getIban() + ".xlsx");
      if (file.exists() && !file.delete()) {
        log.warn("Failed to delete file: {}", file.getAbsolutePath());
      }
      try (
        XSSFWorkbook workbook = xlsProducer.produceReport(statements, account.getCurrencyCode());
        FileOutputStream fo = new FileOutputStream(file)
      ) {
        workbook.write(fo);
        log.info("Saved statements file: {}", file.getAbsolutePath());
      } catch (final IOException ex) {
        log.error("Failed to save statements report to file {}: {}", file, ex.getMessage());
      }
    }
  }

}
