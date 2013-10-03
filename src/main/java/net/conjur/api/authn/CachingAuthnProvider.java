package net.conjur.api.authn;

import net.conjur.util.Args;

/**
 * Caches authn tokens.
 */
public class CachingAuthnProvider implements AuthnProvider {
    private final AuthnProvider base;
    private Token token;

    public CachingAuthnProvider(final AuthnProvider base){
        this.base = Args.notNull(base, "Base");
    }

    public Token authenticate(){
        return authenticate(true);
    }

    public Token authenticate(boolean useCachedToken){
        if(useCachedToken && isValid(token)){
            return token;
        }
        return token = base.authenticate(false);
    }

    private boolean isValid(Token token){
        return token != null && !token.willExpireWithin(60);
    }
}
