package net.conjur.api.authn;

/**
 * Provides Conjur authentication tokens.
 */
public interface AuthnProvider {
    /**
     * Return an authentication token.  This method should be equivalent to {@code authenticate(true)}
     * if the implementation supports token caching.
     *
     * @return a Conjur authentication token.
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
    Token authenticate(boolean useCachedToken);

    /**
     * Returns the username as which this client will authenticate.
     *
     * If we are authenticating as a host, the username is {@code "host/[hostid]"}.
     * @return the username as which this client can authenticate.
     */
    String getUsername();
}
