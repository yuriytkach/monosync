package com.yuriytkach.monosync.service;

import static com.yuriytkach.monosync.service.WiremockMonoApi.TEST_ACCOUNT;
import static com.yuriytkach.monosync.service.WiremockMonoApi.TEST_FROM;
import static com.yuriytkach.monosync.service.WiremockMonoApi.TEST_TO;
import static com.yuriytkach.monosync.service.WiremockMonoApi.TEST_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.yuriytkach.monosync.model.Account;
import com.yuriytkach.monosync.model.Client;
import com.yuriytkach.monosync.model.Statement;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(WiremockMonoApi.class)
class MonoServiceIT {

  @Inject
  MonoService monoService;

  @Test
  void shouldLoadClientInfoForValidToken() {
    final Optional<Client> client = monoService.loadClientInfo(TEST_TOKEN);

    assertThat(client).hasValue(new Client("XXX000YYY", "John Doe", "http://webhook", List.of(
      new Account("accountId1", 42000, 0, 978, "UAH",
        List.of("537541******0001"), "black", "UA00000000000000000000001"),
      new Account("accountId2", 1234567, 0, 980, "UAH",
        List.of("537541******0002"), "black", "UA999999999999999999999991")
    )));
  }

  @ParameterizedTest
  @ValueSource(strings = WiremockMonoApi.BAD_TOKEN)
  @NullSource
  void shouldReturnEmptyOptionalForInvalidToken(final String badToken) {
    final Optional<Client> client = monoService.loadClientInfo(badToken);
    assertThat(client).isEmpty();
  }

  @Test
  void shouldReturnStatementsForValidTokenAccountAndDates() {
    final Optional<List<Statement>> statements = monoService
      .loadStatements(TEST_TOKEN, TEST_ACCOUNT, TEST_FROM, TEST_TO);

    assertThat(statements).isPresent();
    assertThat(statements.get()).containsExactlyInAnyOrder(
      new Statement("id4", 1601575450, "Refill 2", 4829, true, 1000, 1000, 980, 0, 0, 186000),
      new Statement("id3", 1601497803, "Spending 2", 5411, false, -10000, -10000, 980, 0, 8466, 185000),
      new Statement("id2", 1601317914, "Spending 1", 5499, false, -5000, -5000, 980, 0, 499, 195000),
      new Statement("id1", 1601290750, "Refill 1", 4829, false, 100000, 100000, 980, 0, 0, 200000)
    );
  }

  @ParameterizedTest
  @ValueSource(strings = WiremockMonoApi.BAD_TOKEN)
  @NullSource
  void shouldReturnEmptyStatementsForInvalidToken(final String badToken) {
    final Optional<List<Statement>> statements = monoService
      .loadStatements(badToken, TEST_ACCOUNT, TEST_FROM, TEST_TO);

    assertThat(statements).isNotPresent();
  }

}
