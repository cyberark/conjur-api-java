package net.conjur.api;


/**
 * Provides Conjur authentication tokens.
 */
public interface AuthnProvider {

    /**
     * Return an authentication token.  This method should be equivalent to {@code authenticate(true)}
     * if the implementation supports token caching.
     * @return a Conjur authentication token
     */
    Token authenticate();

    /**
     * Return an authentication token.  If the implementation supports token caching
     * and useCachedToken is true, it should return a cached token if a valid (non-expired)
     * one is available.  If useCachedToken is false, it should always fetch a fresh token from
     * the Conjur authn service.
     *
     * @param useCachedToken whether to use a cached token.
     * @return a Conjur authentication token
     */
    // TODO orenbm: Do we need this?
    Token authenticate(boolean useCachedToken);

}
