package com.yuriytkach.monosync.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.slf4j.LoggerFactory;

import com.yuriytkach.monosync.model.Client;
import com.yuriytkach.monosync.model.Statement;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/personal")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "mono")
@RegisterProvider(LoggingFilter.class)
@RegisterProvider(MonoApi.ApiExceptionMapper.class)
public interface MonoApi {

  @GET
  @Path("/client-info")
  @Nullable
  @Retry(delay = 65, delayUnit = ChronoUnit.SECONDS, retryOn = MyTooManyRequestsException.class)
  @Fallback(fallbackMethod = "noClientInfoFallback", applyOn = MyBadRequestException.class)
  Client clientInfo(@HeaderParam("x-token") String token);

  @Nullable
  default Client noClientInfoFallback(final String ignored, final WebApplicationException ex) {
    LoggerFactory.getLogger(MonoApi.class).warn("Cannot get client info because: {}", ex.getMessage());
    return null;
  }

  @GET
  @Path("/statement/{account}/{from}/{to}")
  @Retry(delay = 65, delayUnit = ChronoUnit.SECONDS, retryOn = MyTooManyRequestsException.class)
  @Fallback(fallbackMethod = "noStatementsFallback", applyOn = MyBadRequestException.class)
  List<Statement> statements(
    @HeaderParam("x-token") String token,
    @PathParam("account") String account,
    @PathParam("from") long from,
    @PathParam("to") long to
  );

  default List<Statement> noStatementsFallback(
    final String ignored,
    final String account,
    final long from,
    final long to,
    final WebApplicationException ex
  ) {
    final var fromInstant = from > 1_000_000_000_000L ? Instant.ofEpochMilli(from) : Instant.ofEpochSecond(from);
    final var toInstant = to > 1_000_000_000_000L ? Instant.ofEpochMilli(to) : Instant.ofEpochSecond(to);
    LoggerFactory.getLogger(MonoApi.class).warn(
      "Cannot get statements for account: {} from: {} to: {} because: {}",
      account, fromInstant, toInstant, ex.getMessage()
    );
    return List.of();
  }


  class ApiExceptionMapper implements ResponseExceptionMapper<WebApplicationException> {

    @Override
    public WebApplicationException toThrowable(final Response response) {
      return switch (response.getStatus()) {
        case 429 -> new MyTooManyRequestsException("Too many requests");
        case 400 -> new MyBadRequestException("Bad request");
        default -> new WebApplicationException(response);
      };
    }
  }

  class MyTooManyRequestsException extends WebApplicationException {
    public MyTooManyRequestsException(final String message) {
      super(message, 429);
    }
  }

  class MyBadRequestException extends WebApplicationException {
    public MyBadRequestException(final String message) {
      super(message, 400);
    }
  }

}
