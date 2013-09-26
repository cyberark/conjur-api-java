package net.conjur.api;

import net.conjur.api.authn.Token;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;

public abstract class AuthenticatedClient extends Client {
	private Token token;
	
	public AuthenticatedClient(Token token){
		this.token = token;
	}
	
	// TODO there are obviously more constructors we could support
	
	@Override
	protected RequestBuilder createRequestBuilder(String method, String path){ 
		return super.createRequestBuilder(method, path)
				.addHeader("Authentication", getAuthenticationHeader());
	}
	
	@Override
	protected HttpClient createHttpClient() {
		return createHttpClient(getUsername());
	}
	
	private String getAuthenticationHeader(){
		return getToken().toHeader();
	}
	
	private String getUsername(){
		return getToken().getUsername();
	}

	public Token getToken() {
		return token;
	}
}
