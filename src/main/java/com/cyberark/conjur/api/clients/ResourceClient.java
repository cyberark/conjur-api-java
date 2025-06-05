package com.cyberark.conjur.api.clients;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import com.cyberark.conjur.api.Credentials;
import com.cyberark.conjur.api.Endpoints;
import com.cyberark.conjur.api.ResourceProvider;
import com.cyberark.conjur.api.Token;
import com.cyberark.conjur.util.EncodeUriComponent;
import com.cyberark.conjur.util.rs.TokenAuthFilter;

/**
 * Conjur service client.
 */
public class ResourceClient implements ResourceProvider {

    private WebTarget secrets;
    private final Endpoints endpoints;

    public ResourceClient(final Credentials credentials, final Endpoints endpoints) {
        this(credentials, endpoints, null);
    }

    public ResourceClient(final Credentials credentials,
                          final Endpoints endpoints,
                          final SSLContext sslContext) {
        this.endpoints = endpoints;

        init(credentials, sslContext);
    }

    // Build ResourceClient using a Conjur auth token
    public ResourceClient(final Token token, final Endpoints endpoints) {
        this(token, endpoints, null);
    }

    // Build ResourceClient using a Conjur auth token
    public ResourceClient(final Token token,
                          final Endpoints endpoints,
                          final SSLContext sslContext) {
        this.endpoints = endpoints;

        init(token, sslContext);
    }

    @Override
    public String retrieveSecret(String variableId) {
        Response response = secrets.path(encodeVariableId(variableId))
          .request().get(Response.class);
        validateResponse(response);

        return response.readEntity(String.class);
    }

    @Override
    public void addSecret(String variableId, String secret) {
        Response response = secrets.path(encodeVariableId(variableId)).request()
          .post(Entity.text(secret), Response.class);
        validateResponse(response);
    }

    // The "encodeUriComponent" method encodes plus signs into %2B and spaces
    // into '+'. However, our server decodes plus signs into plus signs in the
    // retrieveSecret request so we need to replace the plus signs (which are
    // spaces) into %20.
    private String encodeVariableId(String variableId) {
        return EncodeUriComponent.encodeUriComponent(variableId).replaceAll("\\+", "%20");
    }

    private Endpoints getEndpoints() {
        return endpoints;
    }

    private void init(Credentials credentials, SSLContext sslContext){
        ClientBuilder builder = ClientBuilder.newBuilder()
                .register(new TokenAuthFilter(new AuthnClient(credentials, endpoints, sslContext)));

                
        if(sslContext != null) {
            builder.sslContext(sslContext);
        }

        Client client = builder.build();

        secrets = client.target(getEndpoints().getSecretsUri());
    }

    private void init(Token token, SSLContext sslContext){
        ClientBuilder builder = ClientBuilder.newBuilder()
                .register(new TokenAuthFilter(new AuthnTokenClient(token)));

        if(sslContext != null) {
            builder.sslContext(sslContext);
        }

        Client client = builder.build();

        secrets = client.target(getEndpoints().getSecretsUri());
    }

    // TODO orenbm: Remove when we have a response filter to handle this
    private void validateResponse(Response response) {
        int status = response.getStatus();
        if (status < 200 || status >= 400) {
            String errorMessage = String.format("Error code: %d, Error message: %s", status, response.readEntity(String.class));
            throw new WebApplicationException(errorMessage, status);
        }
    }
}
