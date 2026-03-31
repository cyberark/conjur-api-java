package com.cyberark.conjur.api;

import com.cyberark.conjur.api.clients.ResourceClient;

import javax.net.ssl.SSLContext;
import java.util.Map;

/**
 * Facade for Conjur variable secret operations.
 *
 * <p>This class provides APIs to retrieve and set individual variable secrets,
 * as well as fetch multiple secrets in a single batch request. Instances are
 * typically obtained via {@link Conjur#variables()}.</p>
 */
public class Variables {

    private final ResourceClient resourceClient;

    /**
     * Create a Variables instance using explicit credentials.
     *
     * @param credentials credentials used to authenticate requests
     * @deprecated Prefer {@link Conjur#variables()} so variables and resources can share a single underlying client.
     */
    @Deprecated
    public Variables(Credentials credentials) {
        this(credentials, null);
    }

    /**
     * Create a Variables instance using explicit credentials and SSL context.
     *
     * @param credentials credentials used to authenticate requests
     * @param sslContext the {@link SSLContext} to use for HTTPS connections
     * @deprecated Prefer {@link Conjur#variables()} so variables and resources can share a single underlying client.
     */
    @Deprecated
    public Variables(Credentials credentials, SSLContext sslContext) {
        resourceClient =
                new ResourceClient(credentials, Endpoints.fromCredentials(credentials), sslContext);
    }

    /**
     * Create a Variables instance using a pre-existing Conjur token.
     *
     * @param token authorization token used for requests
     * @deprecated Prefer {@link Conjur#variables()} so variables and resources can share a single underlying client.
     */
    @Deprecated
    public Variables(Token token) {
        this(token, null);
    }

    /**
     * Create a Variables instance using a pre-existing Conjur token and SSL context.
     *
     * @param token authorization token used for requests
     * @param sslContext the {@link SSLContext} to use for HTTPS connections
     * @deprecated Prefer {@link Conjur#variables()} so variables and resources can share a single underlying client.
     */
    @Deprecated
    public Variables(Token token, SSLContext sslContext) {
        resourceClient = new ResourceClient(token, Endpoints.fromSystemProperties(), sslContext);
    }

    Variables(ResourceClient resourceClient) {
        this.resourceClient = resourceClient;
    }

    /**
     * Retrieve the secret value for a Conjur variable.
     *
     * @param variableId variable identifier (without account/kind prefix)
     * @return secret value stored in the specified variable
     */
    public String retrieveSecret(String variableId) {
        return resourceClient.retrieveSecret(variableId);
    }

    /**
     * Set the secret value for a Conjur variable.
     *
     * @param variableId variable identifier (without account/kind prefix)
     * @param secret secret value to store
     */
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
