package com.cyberark.conjur.api;

import javax.net.ssl.SSLContext;
import java.util.List;
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

    /**
     * List all resources visible to the authenticated identity.
     *
     * @return list of all resources
     */
    public List<ConjurResource> listResources() {
        return resourceClient.listResources();
    }

    /**
     * List resources filtered by kind.
     *
     * @param kind the resource kind (e.g. "variable", "host")
     * @return resources matching the given kind
     */
    public List<ConjurResource> listResources(String kind) {
        return resourceClient.listResources(kind);
    }

    /**
     * List resources with full query parameter control.
     *
     * @param kind   resource kind filter (null for all kinds)
     * @param search text search filter (null for no search)
     * @param limit  max results (null for server default)
     * @param offset pagination offset (null for no offset)
     * @return resources matching the query
     */
    public List<ConjurResource> listResources(String kind, String search, Integer limit, Integer offset) {
        return resourceClient.listResources(kind, search, limit, offset);
    }

    /**
     * Count resources visible to the authenticated identity.
     *
     * @param kind   resource kind filter (null for all kinds)
     * @param search text search filter (null for no search)
     * @return the number of matching resources
     */
    public int countResources(String kind, String search) {
        return resourceClient.countResources(kind, search);
    }
}
