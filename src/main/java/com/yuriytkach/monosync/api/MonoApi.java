package com.yuriytkach.monosync.api;

import java.util.List;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.yuriytkach.monosync.model.Client;
import com.yuriytkach.monosync.model.Statement;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/personal")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "mono")
@RegisterProvider(LoggingFilter.class)
public interface MonoApi {

  @GET
  @Path("/client-info")
  Client clientInfo(@HeaderParam("x-token") String token);

  @GET
  @Path("/statement/{account}/{from}/{to}")
  List<Statement> statements(
    @HeaderParam("x-token") String token,
    @PathParam("account") String account,
    @PathParam("from") long from,
    @PathParam("to") long to
  );

}
