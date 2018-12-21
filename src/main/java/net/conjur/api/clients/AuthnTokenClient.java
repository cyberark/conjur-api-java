package net.conjur.api.clients;
import net.conjur.api.AuthnProvider;
import net.conjur.api.Token;

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
