package net.conjur.util.rs;

import net.conjur.api.AuthnProvider;
import net.conjur.api.Token;
import net.conjur.util.Args;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * Filter to add Conjur authentication tokens to requests.
 */
public class TokenAuthFilter implements ClientRequestFilter {

    private static final int EXPIRATION_TIME_BUFFER = 2 * 60;
    private static final String HEADER = "Authorization";

    private final AuthnProvider authn;
    private Token currentToken;

    public TokenAuthFilter(final AuthnProvider authn){
        this.authn = Args.notNull(authn);
    }

    public void filter(ClientRequestContext rc) throws IOException {
        if (!isTokenValid()) {
            currentToken = authn.authenticate();
        }

        rc.getHeaders().putSingle(HEADER, currentToken.toHeader());
    }

    private boolean isTokenValid() {
        return currentToken != null && !currentToken.willExpireWithin(EXPIRATION_TIME_BUFFER);
    }
}