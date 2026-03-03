package com.cyberark.conjur.api;

import javax.net.ssl.SSLContext;
import java.util.Map;

import com.cyberark.conjur.api.clients.ResourceClient;

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

    /**
     * Fetch multiple secret values in one invocation.
     *
     * @param variableIds the variable IDs to retrieve
     * @return a map of variable ID to secret value
     */
    public Map<String, String> retrieveBatchSecrets(String... variableIds) {
        return resourceClient.retrieveBatchSecrets(variableIds);
    }
}
