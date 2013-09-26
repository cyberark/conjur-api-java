package net.conjur.api.authn;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.conjur.api.Client;
import net.conjur.api.ConjurApiException;
import net.conjur.api.Endpoints;
import net.conjur.api.HttpStatusException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class Authn extends Client {

	private static String LOGIN_PATH = "/users/login";
	private static String AUTHENTICATE_PATH = "/users/%s/login";
	
	private static String authenticatePath(String username){
		return String.format(AUTHENTICATE_PATH, fullyEscape(username));
	}
	
	private String endpoint;
	
	public Authn(String endpoint){
		this.endpoint = endpoint;
	}
	
	public Authn(URI endpoint){
		this(endpoint.toString());
	}
	
	public Authn(Endpoints endpoints){
		this(endpoints.authn());
	}
	
	/**
	 * Exchange a username/apiKey pair for a token that can be used to authenticate
	 * future API calls.
	 * @param username a conjur username
	 * @param apiKey an api key as returned by {@link #authenticate(String, String)}
	 * @return a base64 encoded string that can be used to authenticate api calls.
	 */
	public Token authenticate(String username, String apiKey) throws ConjurApiException{
		try{
			return authenticateThrowing(username, apiKey);
		}catch(HttpStatusException e){
			if(e.getStatusCode() == 401){
				throw  new AuthenticationFailure(e);
			}
			throw new ConjurApiException(e);
		}catch(IOException e){
			throw new ConjurApiException(e);
		} catch (URISyntaxException e) {
			throw new ConjurApiException(e);
		}
	}
	
	private Token authenticateThrowing(String username, String apiKey) throws IOException, URISyntaxException{
		HttpClient client = createHttpClient();
		HttpUriRequest request = createRequestBuilder("POST",  authenticatePath(username))
				.setHeader("Content-Type", "text/plain")
				.setEntity(new StringEntity(apiKey)).build();
		return Token.fromJson(responseJson(client, request));
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
				throw new AuthenticationFailure(e);
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
		HttpClient client = createHttpClient(username, password);
		HttpUriRequest get = createRequest("GET", LOGIN_PATH);
		return responseString(client, get);
	}
	
	@Override
	protected String getEndpoint() {
		return this.endpoint;
	}


}
