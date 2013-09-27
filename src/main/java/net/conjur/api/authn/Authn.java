package net.conjur.api.authn;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.conjur.api.Client;
import net.conjur.api.ConjurApiException;
import net.conjur.api.Endpoints;
import net.conjur.api.HttpStatusException;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.BasicHttpContext;

public class Authn extends Client {

	private static String LOGIN_PATH = "/users/login";
	private static String AUTHENTICATE_PATH = "/users/%s/authenticate";
	
	private static String authenticatePath(String username){
		return String.format(AUTHENTICATE_PATH, fullyEscape(username));
	}
	
	public Authn(URI endpoint){
		super(endpoint);
	}
	
	public Authn(String endpoint){
		super(endpoint);
	}
	
	public Authn(Endpoints endpoints){
		this(endpoints.authn());
	}
	
	/**
	 * Exchange a username/apiKey pair for a token that can be used to authenticate
	 * future API calls.
	 * @param username a conjur username
	 * @param apiKey an api key as returned by {@link #login(String, String)}
	 * @return a Token that can be used to create authenticated clients
	 */
	public Token authenticate(String username, String apiKey) throws ConjurApiException{
		try{
			return authenticateThrowing(username, apiKey);
		}catch(HttpStatusException e){
			if(e.getStatusCode() == 401)
				throw  new AuthenticationFailureException(e);
			throw new ConjurApiException(e);
		}catch(IOException e){
			throw new ConjurApiException(e);
		}
	}
	
	private Token authenticateThrowing(String username, String apiKey) throws IOException {
		HttpUriRequest request = requestBuilder("POST",  authenticatePath(username))
				.addHeader("Content-Type", "text/plain")
				.setEntity(new StringEntity(apiKey))
				.build();
		return Token.fromJson(execute(request));
	}
	
	/**
	 * Exchange a Conjur username and password for an API key
	 * @param username the conjur username
	 * @param password the password for the given username
	 * @return an API key
	 * @throws ConjurApiException when something goes wrong other than a failed login
	 */
	public String login(String username, String password) throws ConjurApiException{
		try{
			return loginThrowing(username, password);
		}catch(HttpStatusException e){
			if(e.getStatusCode() == 401){
				throw new AuthenticationFailureException(e);
			}else{
				throw new ConjurApiException(e);
			}
		}catch(IOException e){
			throw new ConjurApiException(e);
		}catch(URISyntaxException e){
			throw new ConjurApiException(e);
		}
	}
	
	private String loginThrowing(String username, String password) throws IOException, URISyntaxException{
		HttpUriRequest request = request("GET", LOGIN_PATH);
		request.addHeader(basicAuthHeader(request, username, password));
		return execute(request);
	}
	
	// Since conjur services don't issue auth challenges at the moment we have to cobble together
	// our own auth header instead of relying on the facilities provided by the httpclient library.
	private Header basicAuthHeader(HttpRequest request, String username, String password){
		try {
			return new BasicScheme().authenticate(
					new UsernamePasswordCredentials(username, password), 
					request, 
					new BasicHttpContext());
		} catch (AuthenticationException e) {
			// If you look at the method in question, it *does not* throw this exception!
			throw new RuntimeException(e);
		}
	}
}
