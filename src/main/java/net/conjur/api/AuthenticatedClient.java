package net.conjur.api;

import net.conjur.api.authn.Token;
import net.conjur.util.Args;

import org.apache.http.client.fluent.Request;

/**
 * Abstract base class for Conjur clients that make requests using an authentication 
 * token.
 */
public abstract class AuthenticatedClient extends Client {

	private Token token;
	
	/**
	 * Set the token used to authenticate requests made by this client.
	 * @param token May not be null.
	 */
	public void setToken(Token token){
		this.token = Args.notNull(token, "token");;
	}
	
	/**
	 * @return The token used to authenticate requests made by this client.
	 */
	public Token getToken() {
		return token;
	}
	
	/**
	 * Construct a client from endpoint configuration and token.
	 * @see Client#Client(Endpoints)
	 * @param endpoints The endpoint configuration.
	 * @param token The token, which must not be null.
	 */
	public AuthenticatedClient(Endpoints endpoints, Token token) {
		super(endpoints);
		setToken(token);
	}

    @Override
    protected Request prepareRequest(Request request) {
        return request.addHeader(getToken().toHeader());
    }
}
