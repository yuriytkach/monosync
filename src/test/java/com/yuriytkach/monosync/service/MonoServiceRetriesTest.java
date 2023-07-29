package com.yuriytkach.monosync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import com.yuriytkach.monosync.api.MonoApi;
import com.yuriytkach.monosync.model.ApiError;
import com.yuriytkach.monosync.model.Client;

@ExtendWith(MockitoExtension.class)
public class MonoServiceRetriesTest {

  @Mock
  MonoApi monoApi;

  @InjectMocks
  MonoService monoService;

  public static Stream<Arguments> shouldRetryWithSleep() {
    final Function<MonoApi, OngoingStubbing<Object>> mockFuncForClientInfo = api -> when(api.clientInfo("token"));
    final Function<MonoService, Optional<?>> loadClientInfoFunc = service -> service.loadClientInfo("token");

    final Function<MonoApi, OngoingStubbing<Object>> mockFuncForStatements = api ->
      when(api.statements(eq("token"), eq("accountId"), anyLong(), anyLong()));
    final Function<MonoService, Optional<?>> loadStatementsFunc = service -> service
      .loadStatements("token", "accountId", LocalDate.now(), LocalDate.now());

    return Stream.of(
      Arguments.of(
        mockFuncForClientInfo,
        loadClientInfoFunc,
        new Client("id", "name", "hook", List.of())
      ),
      Arguments.of(
        mockFuncForStatements,
        loadStatementsFunc,
        List.of()
      )
    );
  }

  @ParameterizedTest
  @MethodSource
  void shouldRetryWithSleep(
    final Function<MonoApi, OngoingStubbing<Object>> apiStubFunc,
    final Function<MonoService, Optional<Object>> serviceFunction,
    final Object expectedResult
  ) {
    monoService.sleepDuration = Duration.of(10, ChronoUnit.MILLIS);
    monoService.maxRetries = 2;

    final Response apiResponse = Response.status(429).entity(new ApiError("error")).build();
    final WebApplicationException exception = new WebApplicationException(apiResponse);

    apiStubFunc.apply(monoApi)
      .thenThrow(exception)
      .thenThrow(exception)
      .thenReturn(expectedResult);

    final Optional<Object> response = serviceFunction.apply(monoService);

    assertThat(response).hasValue(expectedResult);
  }

}
