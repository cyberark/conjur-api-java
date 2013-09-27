package net.conjur.api.authn;

import java.net.URI;

import net.conjur.api.Client;
import net.conjur.api.Endpoints;
import net.conjur.util.HttpHelpers;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * Conjur authentication service client.
 * 
 * This client provides methods to get API tokens from the conjur authentication service,
 * which can then be used to make authenticated calls to other conjur services.
 *
 */
public class AuthnClient extends Client {

	public AuthnClient(Endpoints endpoints) {
		super(endpoints);
	}

	public AuthnClient(String endpoint) {
		super(endpoint);
	}

	public AuthnClient(URI endpoint) {
		super(endpoint);
	}

	private static String LOGIN_PATH = "/users/login";
	private static String AUTHENTICATE_PATH = "/users/%s/authenticate";
	
	private static String authenticatePath(String username){
		return String.format(AUTHENTICATE_PATH, username);
	}

	/**
	 * Exchange a username/apiKey pair for a token that can be used to authenticate
	 * future API calls.
	 * @param username a conjur username
	 * @param apiKey an api key as returned by {@link #login(String, String)}
	 * @return a Token that can be used to create authenticated clients
	 */
	public Token authenticate(String username, String apiKey) {
		HttpUriRequest request = requestBuilder("POST",  authenticatePath(username))
				.setEntity(HttpHelpers.stringEntity(apiKey))
				.build();
		return Token.fromJson(execute(request));
	}
	
	/**
	 * Exchange a Conjur username and password for an API key.
	 * 
	 * @param username the conjur username
	 * @param password the password for the given username
	 * @return an API key
	 */
	public String login(String username, String password){
		HttpUriRequest request = requestBuilder("GET", LOGIN_PATH)
				.addHeader(HttpHelpers.basicAuthHeader(username, password))
				.build();
		return execute(request);
	}
	
	@Override
	protected String getEndpointFromEndpoints(Endpoints endpoints) {
		return endpoints.authn();
	}
}
