package net.conjur.api.directory;

import java.io.IOException;
import java.net.URI;

import org.apache.http.client.methods.HttpUriRequest;

import net.conjur.api.AuthenticatedClient;
import net.conjur.api.Endpoints;
import net.conjur.api.User;
import net.conjur.api.authn.Token;

import com.google.gson.JsonElement;

public class Directory extends AuthenticatedClient {
	private static final String USERS_PATH = "/users";
	
	public Directory(Endpoints endpoints, Token token) {
		super(endpoints.directory(), token);
	}
	
	public Directory(URI endpoint, Token token){
		super(endpoint, token);
	}
	
	public Directory(String endpoint, Token token){
		super(endpoint, token);
	}
	
	public User createUser(String username) throws IOException{
		HttpUriRequest request = requestBuilder("POST", USERS_PATH).addParameter("login", username).build();
		return User.fromJson(execute(request));
	}

}
