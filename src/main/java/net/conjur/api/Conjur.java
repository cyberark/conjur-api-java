package net.conjur.api;

import net.conjur.api.authn.AuthnClient;
import net.conjur.api.authn.AuthnProvider;

/**
 * Entry point for the Conjur API client.
 * 
 * <p>This class provides access to various Conjur resources, using
 * a particular set of endpoints and authorization or credentials</p>
 */
public class Conjur extends Resource {
    private Users users;
    private Variables variables;

    /**
     * @param authn used to authenticate requests to Conjur services
     * @param endpoints locates Conjur services
     */
    public Conjur(AuthnProvider authn, Endpoints endpoints) {
        super(authn, endpoints);
        init();
    }

    /**
     * Create a Conjur instance that uses the given AuthnProvider and 
     * {@link Endpoints#getDefault()}.
     * @param authn used to authenticate requests to Conjur services
     */
    public Conjur(AuthnProvider authn){
        this(authn, Endpoints.getDefault());
    }

    /**
     * Create a Conjur instance that uses an AuthnClient constructed with
     * the given credentials and the given endpoints
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     * @param endpoints locates Conjur services
     */
    public Conjur(String username, String password, Endpoints endpoints){
        this(new AuthnClient(username, password, endpoints), endpoints);
    }

    /**
     * Create a Conjur instance that uses an AuthnClient constructed with
     * the given credentials and the default endpoints. 
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     */
    public Conjur(String username, String password){
        this(new AuthnClient(username, password));
    }

    /**
     * Create a Conjur instance that uses an AuthnClient constructed with
     * the given credentials and the default endpoints.
     * @param credentials the conjur identity to authenticate as
     */
    public Conjur(Credentials credentials){
        this(credentials.getUsername(), credentials.getPassword());
    }

    /**
     * Create a Conjur instance that uses an AuthnClient constructed with
     * the given credentials and the default endpoints.
     * @param credentials the conjur identity to authenticate as
     */
    public Conjur(Credentials credentials, Endpoints endpoints){
        this(credentials.getUsername(), credentials.getPassword(), endpoints);
    }

    /**
     * Get a Users instance configured with the same parameters as this instance.
     * @return the users instance
     */
    public Users users(){
        return users;
    }

    /**
     * Get a Variables instance configured with the same parameters as this instance.
     * @return the variables instance
     */
    public Variables variables(){
        return variables;
    }

    /**
     * Get the authentication provider for this instance.
     * @return the authentication provider
     */
    public AuthnProvider getAuthn(){
        return super.getAuthn();
    }

    private void init(){
        users = new Users(this);
        variables = new Variables(this);
    }

}
