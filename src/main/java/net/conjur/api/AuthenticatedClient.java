package net.conjur.api;

import java.net.URI;

import net.conjur.api.authn.Token;

import org.apache.http.client.methods.RequestBuilder;

public abstract class AuthenticatedClient extends Client {
	private final Token token;
	
	public AuthenticatedClient(String endpoint, Token token){
		super(endpoint);
		this.token = token;
	}
	
	public AuthenticatedClient(URI endpoint, Token token){
		super(endpoint);
		this.token = token;
	}
	
	// TODO there are obviously more constructors we could support
	
	@Override
	protected RequestBuilder requestBuilder(String method, String path) {
		return super.requestBuilder(method, path)
				.addHeader("Authorization", getAuthenticationHeader());
	}
	
	private String getAuthenticationHeader(){
		return getToken().toHeader();
	}
	
	public Token getToken() {
		return token;
	}
}
