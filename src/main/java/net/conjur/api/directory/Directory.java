package net.conjur.api.directory;

import java.io.IOException;

import net.conjur.api.AuthenticatedClient;
import net.conjur.api.Endpoints;
import net.conjur.api.User;
import net.conjur.api.authn.Token;

import com.google.gson.JsonElement;

public class Directory extends AuthenticatedClient {
	private static final String USERS_PATH = "/users";
	private String endpoint;
	
	public Directory(Endpoints endpoints, Token token) {
		this(endpoints.directory(), token);
	}
	
	public Directory(String endpoint, Token token){
		super(token);
		this.endpoint  = endpoint;
	}
	
	public User createUser(String username) throws IOException{
		JsonElement json = responseJson(createRequestBuilder("POST", USERS_PATH).addParameter("login", username).build());
		return User.fromJson(json);
	}

	@Override
	protected String getEndpoint() {
		return endpoint;
	}

}
