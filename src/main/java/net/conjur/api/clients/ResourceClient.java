package net.conjur.api.clients;

import net.conjur.api.Endpoints;
import net.conjur.api.ResourceProvider;
import net.conjur.util.EncodeUriComponent;
import net.conjur.util.rs.TokenAuthFilter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Conjur service client.
 */
public class ResourceClient implements ResourceProvider {

    private WebTarget secrets;
    private final Endpoints endpoints;

	public ResourceClient(final String username, final String password, final Endpoints endpoints) {
        this.endpoints = endpoints;

        init(username, password);
	}

    public String retrieveSecret(String variableId) {
        Response response = secrets.path(variableId).request().get(Response.class);
        validateResponse(response);

        return response.readEntity(String.class);
    }

    public void addSecret(String variableId, String secret) {
        Response response = secrets.path(EncodeUriComponent.encodeUriComponent(variableId)).request().post(Entity.text(secret), Response.class);
        validateResponse(response);
    }

    private Endpoints getEndpoints() {
        return endpoints;
    }

    private void init(String username, String password){
        final ClientBuilder builder = ClientBuilder.newBuilder()
                .register(new TokenAuthFilter(new AuthnClient(username, password, endpoints)));

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
