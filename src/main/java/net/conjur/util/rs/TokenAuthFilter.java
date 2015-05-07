package net.conjur.util.rs;

import net.conjur.api.authn.AuthnProvider;
import net.conjur.util.Args;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * Filter to add Conjur authentication tokens to requests.
 */
public class TokenAuthFilter implements ClientRequestFilter {
    private static final String HEADER = "Authorization";
    private final AuthnProvider authn;

    public TokenAuthFilter(final AuthnProvider authn){
        this.authn = Args.notNull(authn);
    }

    public void filter(ClientRequestContext rc) throws IOException {
        rc.getHeaders().putSingle(HEADER, authn.authenticate().toHeader());
    }
}
