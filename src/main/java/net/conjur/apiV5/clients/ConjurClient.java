package net.conjur.apiV5.clients;

import net.conjur.apiV5.Endpoints;
import net.conjur.util.HostNameVerification;
import net.conjur.util.rs.HttpBasicAuthFilter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

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
}
