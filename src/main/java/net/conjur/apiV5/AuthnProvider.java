package net.conjur.apiV5;


/**
 * Provides Conjur authentication tokens.
 */
public interface AuthnProvider {

    /**
     * Authenticate the api key with
     * @param apiKey - the key we need to obtain an access token
     * @return an access token for future requests
     */
    Token authenticate(String apiKey);

    /**
     * Return an authentication token.  If the implementation supports token caching
     * and useCachedToken is true, it should return a cached token if a valid (non-expired)
     * one is available.  If useCachedToken is false, it should always fetch a fresh token from
     * the Conjur authn service.
     *
     * @param apiKey - the key we need to obtain an access token
     * @param useCachedToken whether to use a cached token.
     * @return a Conjur authentication token
     */
    // TODO orenbm: Do we need this?
    Token authenticate(String apiKey, boolean useCachedToken);

    /**
     * Login to a Conjur account with the credentials specified in the configuration
     * @return The API key of the user
     */
    String login();


}
