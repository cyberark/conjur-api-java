package com.cyberark.conjur.api;

import javax.net.ssl.SSLContext;
import java.util.List;
import java.util.Map;

/**
 * Entry point for the Conjur API client.
 */
public class Conjur {

    private Variables variables;

    /**
     * Create a Conjur instance that uses credentials from the system properties
     */
    public Conjur(){
        this(Credentials.fromSystemProperties());
    }

    /**
     * Create a Conjur instance that uses credentials from the system properties
     * @param sslContext the {@link SSLContext} to use for connections to Conjur server
     */
    public Conjur(SSLContext sslContext){
        this(Credentials.fromSystemProperties(), sslContext);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     */
    public Conjur(String username, String password) {
        this(new Credentials(username, password));
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     * @param sslContext the {@link SSLContext} to use for connections to Conjur server
     */
    public Conjur(String username, String password, SSLContext sslContext) {
        this(new Credentials(username, password), sslContext);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     * @param authnUrl the conjur authentication url
     */
    public Conjur(String username, String password, String authnUrl) {
        this(new Credentials(username, password, authnUrl));
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     * @param authnUrl the conjur authentication url
     * @param sslContext the {@link SSLContext} to use for connections to Conjur server
     */
    public Conjur(String username, String password, String authnUrl, SSLContext sslContext) {
        this(new Credentials(username, password, authnUrl), sslContext);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param credentials the conjur identity to authenticate as
     */
    public Conjur(Credentials credentials) {
        this(credentials, null);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param credentials the conjur identity to authenticate as
     * @param sslContext the {@link SSLContext} to use for connections to Conjur server
     */
    public Conjur(Credentials credentials, SSLContext sslContext) {
        variables = new Variables(credentials, sslContext);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param token the conjur authorization token to use
     */
    public Conjur(Token token) {
        this(token, null);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param token the conjur authorization token to use
     * @param sslContext the {@link SSLContext} to use for connections to Conjur server
     */
    public Conjur(Token token, SSLContext sslContext) {
        variables = new Variables(token, sslContext);
    }

    /**
     * Get a Variables instance configured with the same parameters as this instance.
     * @return the variables instance
     */
    public Variables variables() {
        return variables;
    }

    /**
     * Fetch multiple secret values in one invocation.
     *
     * @param variableIds the variable IDs to retrieve
     * @return a map of variable ID to secret value
     */
    public Map<String, String> retrieveBatchSecrets(String... variableIds) {
        return variables.retrieveBatchSecrets(variableIds);
    }

    /**
     * List all resources visible to the authenticated identity.
     *
     * @return list of all resources
     */
    public List<ConjurResource> listResources() {
        return variables.listResources();
    }

    /**
     * List resources filtered by kind.
     *
     * @param kind the resource kind (e.g. "variable", "host")
     * @return resources matching the given kind
     */
    public List<ConjurResource> listResources(String kind) {
        return variables.listResources(kind);
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
        return variables.listResources(kind, search, limit, offset);
    }

    /**
     * Count resources visible to the authenticated identity.
     *
     * @param kind   resource kind filter (null for all kinds)
     * @param search text search filter (null for no search)
     * @return the number of matching resources
     */
    public int countResources(String kind, String search) {
        return variables.countResources(kind, search);
    }
}
