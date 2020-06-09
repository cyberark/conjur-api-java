package com.cyberark.conjur.api.clients;
import com.cyberark.conjur.api.AuthnProvider;
import com.cyberark.conjur.api.Token;

public class AuthnTokenClient implements AuthnProvider {
    private Token token;

    public AuthnTokenClient(Token token) {
        this.token = token;
    }

    public Token authenticate() {
        return token;
    }

    public Token authenticate(boolean useCachedToken) {
        return this.authenticate();
    }
}
