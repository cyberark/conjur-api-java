package net.conjur.api;

import java.net.URI;

import net.conjur.api.authn.Token;
import net.conjur.util.Args;

import org.apache.http.client.methods.RequestBuilder;

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
		Args.notNull(token, "token");
		this.token = token;
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

	/**
	 * Construct a client from endpoint URI String
	 * @see Client#Client(String)
	 * @param endpoint The endpoint URI as a String.
	 * @param token The token, which must not be null.
	 */
	public AuthenticatedClient(String endpoint, Token token) {
		super(endpoint);
		setToken(token);
	}

	/**
	 * Construct a client from endpoint URI and token.
	 * @see Client#Client(URI)
	 * @param endpoints The endpoint URI.
	 * @param token The token, which must not be null.
	 */
	public AuthenticatedClient(URI endpoint, Token token) {
		super(endpoint);
		setToken(token);
	}

	@Override
	protected RequestBuilder requestBuilder(String method, String path) {
		return super.requestBuilder(method, path)
				.addHeader(getToken().toHttpHeader());
	}
}
