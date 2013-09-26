package net.conjur.api.authn;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.conjur.api.Client;
import net.conjur.api.ConjurApiException;
import net.conjur.api.Endpoints;
import net.conjur.api.HttpStatusException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

public class Authn extends Client {

	private String endpoint;
	
	public Authn(String endpoint){
		this.endpoint = endpoint;
	}
	
	public Authn(URI endpoint){
		this(endpoint.toString());
	}
	
	public Authn(Endpoints endpoints, String account){
		this(endpoints.authn());
	}
	
	/**
	 * Exchange a username/apiKey pair for a token that can be used to authenticate
	 * future API calls.
	 * @param username a conjur username
	 * @param apiKey an api key as returned by {@link #authenticate(String, String)}
	 * @return a base64 encoded string that can be used to authenticate api calls.
	 */
	public String authenticate(String username, String apiKey) throws ConjurApiException{
		try{
			return authenticateThrowing(username, apiKey);
		}catch(IOException e){
			throw new ConjurApiException(e);
		} catch (URISyntaxException e) {
			throw new ConjurApiException(e);
		}catch(HttpStatusException e){
			if(e.getStatusCode() == 401){
				throw  new AuthenticationFailure(e);
			}
			throw new ConjurApiException(e);
		}
	}
	
	private String authenticateThrowing(String username, String apiKey) throws IOException, HttpStatusException, URISyntaxException{
		CloseableHttpClient client = defaultHttpClient();
		URI uri = new URIBuilder(getURI()).setPath("users/" + fullyEscape(username) + "/authenticate")
				.build();
		HttpPost post = new HttpPost(uri);
		post.setHeader("Content-Type", "text/plain");
		post.setEntity(new StringEntity(apiKey));
		return Base64.encodeBase64URLSafeString(requestString(client, post).getBytes());
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
		}catch(IOException e){
			throw new ConjurApiException(e);
		}catch(URISyntaxException e){
			throw new ConjurApiException(e);
		}catch(HttpStatusException e){
			if(e.getStatusCode() == 401){
				throw new AuthenticationFailure(e);
			}else{
				throw new ConjurApiException(e);
			}
		}
	}
	
	private String loginThrowing(String username, String password) throws IOException, URISyntaxException, HttpStatusException{
		CloseableHttpClient client = httpClientWithBasicAuth(username, password);
		URI uri = new URIBuilder(getURI()).setPath("users/login").build();
		HttpGet get = new HttpGet(uri);
		return requestString(client, get);
	}
	
	@Override
	protected String getEndpoint() {
		return this.endpoint;
	}


}
