package net.conjur.api;

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
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     */
    public Conjur(String username, String password) {
        this(new Credentials(username, password));
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param credentials the conjur identity to authenticate as
     */
    public Conjur(Credentials credentials) {
        variables = new Variables(credentials);
    }

    /**
     * Get a Variables instance configured with the same parameters as this instance.
     * @return the variables instance
     */
    public Variables variables() {
        return variables;
    }

}
