package com.yuriytkach.monosync.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.yuriytkach.monosync.model.Client;
import com.yuriytkach.monosync.model.Statement;

@Path("/personal")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "networks.mono")
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
