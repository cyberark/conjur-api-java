package net.conjur.api.authn;

import net.conjur.util.Args;

/**
 * This class provides authentication with a fixed token.  This
 * can be used for example if you have a token from an HTTP request
 * header and want to make requests on behalf of the role for which that
 * token was issued.
 */
public class TokenAuthnProvider implements AuthnProvider {
    private final Token token;

    public static TokenAuthnProvider fromHeaderValue(String authorizationHeaderValue){
        return new TokenAuthnProvider(Token.fromHeaderValue(authorizationHeaderValue));
    }

    public TokenAuthnProvider(Token token){
        this.token = Args.notNull(token, "token");
    }

    public Token authenticate(){
        return token;
    }

    public Token authenticate(boolean useCachedToken){
        return token;
    }

    public String getUsername(){
        return token.getData();
    }

}
