package net.conjur.api.clients;

import net.conjur.api.Endpoints;
import net.conjur.util.HostNameVerification;
import net.conjur.util.rs.HttpBasicAuthFilter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

public class ConjurClient {

    private final Client client;
    private final Endpoints endpoints;

    protected ConjurClient(final String username, final String password, final Endpoints endpoints) {
        final ClientBuilder builder = ClientBuilder.newBuilder()
                .register(new HttpBasicAuthFilter(username, password));

        HostNameVerification.getInstance().updateClientBuilder(builder);

        client = builder.build();

        this.endpoints = endpoints;
    }

    protected Client getClient() {
        return client;
    }

    protected Endpoints getEndpoints() {
        return endpoints;
    }

    protected void validateResponse(Response res) throws WebApplicationException {
        int status = res.getStatus();
        if (status < 200 || status >= 400) {
            String errorMessage = String.format("Error code: %d, Error message: %s", status, res.readEntity(String.class));
            throw new WebApplicationException(errorMessage, status);
        }
    }
}
