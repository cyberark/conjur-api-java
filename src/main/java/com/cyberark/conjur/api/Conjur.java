package com.cyberark.conjur.api;

import javax.net.ssl.SSLContext;

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
}
