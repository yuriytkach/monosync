package com.yuriytkach.monosync.service;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.forbidden;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.time.ZoneId.systemDefault;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class WiremockMonoApi implements QuarkusTestResourceLifecycleManager {

  public static final String TEST_TOKEN = "test-token";
  public static final String BAD_TOKEN = "bad-token";

  public static final String TEST_ACCOUNT = "test-account";
  public static final LocalDate TEST_FROM = LocalDate.now().minus(2, ChronoUnit.DAYS);
  public static final long TEST_FROM_TIMESTAMP = TEST_FROM.atStartOfDay(systemDefault()).toInstant().toEpochMilli();
  public static final LocalDate TEST_TO = LocalDate.now();
  public static final long TEST_TO_TIMESTAMP = TEST_TO.atStartOfDay(systemDefault())
    .toInstant().toEpochMilli();

  private WireMockServer wireMockServer;

  @Override
  public Map<String, String> start() {
    wireMockServer = new WireMockServer();
    wireMockServer.start();

    stubFor(get(urlEqualTo("/personal/client-info"))
      .withHeader("x-token", equalTo(TEST_TOKEN))
      .willReturn(ok()
        .withHeader("Content-Type", "application/json")
        .withBodyFile("mocked-responses/client-info.json")
      ));

    stubFor(get(urlEqualTo("/personal/statement/" + TEST_ACCOUNT + "/" + TEST_FROM_TIMESTAMP + "/" + TEST_TO_TIMESTAMP))
      .withHeader("x-token", equalTo(TEST_TOKEN))
      .willReturn(ok()
        .withHeader("Content-Type", "application/json")
        .withBodyFile("mocked-responses/statements.json")
      ));

    stubFor(get(anyUrl())
      .withHeader("x-token", absent())
      .willReturn(badRequest()
        .withHeader("Content-Type", "application/json")
        .withBodyFile("mocked-responses/bad-request.json")
      ));

    stubFor(get(anyUrl())
      .withHeader("x-token", equalTo(BAD_TOKEN))
      .willReturn(forbidden()
        .withHeader("Content-Type", "application/json")
        .withBodyFile("mocked-responses/forbidden.json")
      ));

    return Collections.singletonMap("networks.mono/mp-rest/url", wireMockServer.baseUrl());
  }

  @Override
  public void stop() {
    if (null != wireMockServer) {
      wireMockServer.stop();
    }
  }
}
