package net.conjur.api;

import javax.net.ssl.SSLContext;

import net.conjur.api.clients.ResourceClient;

public class Variables {

    private ResourceClient resourceClient;

    public Variables(Credentials credentials) {
        this(credentials, null);
    }

    public Variables(Credentials credentials, SSLContext sslContext) {
        resourceClient =
                new ResourceClient(credentials, Endpoints.fromCredentials(credentials), sslContext);
    }

    public Variables(Token token) {
        this(token, null);
    }

    public Variables(Token token, SSLContext sslContext) {
        resourceClient = new ResourceClient(token, Endpoints.fromSystemProperties(), sslContext);
    }

    public String retrieveSecret(String variableId) {
        return resourceClient.retrieveSecret(variableId);
    }

    public void addSecret(String variableId, String secret){
        resourceClient.addSecret(variableId, secret);
    }
}
