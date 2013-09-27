package net.conjur.api.directory;

import java.io.IOException;
import java.net.URI;

import net.conjur.api.AuthenticatedClient;
import net.conjur.api.Endpoints;
import net.conjur.api.authn.Token;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

public class Directory extends AuthenticatedClient {
	private static final String USERS_PATH = "/users";
	private static final String VARIABLES_PATH = "/variables";
	
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

	public Variable createVariable(String kind, String mimeType, String id) throws IOException {
		RequestBuilder builder = requestBuilder("POST", VARIABLES_PATH)
				.addParameter("mime_type", mimeType)
				.addParameter("kind", kind);
		
		if(id != null){
			builder.addParameter("id", id);
		}
		
		return Variable.fromJson(this, execute(builder.build()));
	}
	
	public void addVariableValue(String variableId, String value) throws IOException {
		HttpUriRequest req = requestBuilder("POST", variableValuesPath(variableId))
				.addParameter("value", value)
				.build();
		execute(req);
	}
	
	public String getVariableValue(String variableId) throws IOException{
		HttpUriRequest req = request("GET", variableValuePath(variableId));
		return execute(req);
	}
	
	public String getVariableValue(String variableId, int version) throws IOException{
		HttpUriRequest req = requestBuilder("GET",  variableValuePath(variableId))
				.addParameter("version", String.valueOf(version))
				.build();
		return execute(req);
	}
	
	public Variable getVariable(String id) throws IOException {
		HttpUriRequest req = request("GET", variablePath(id));
		return Variable.fromJson(this, execute(req));
	}
	

	private String variableValuesPath(String variableId){ 
		return variablePath(variableId) + "/values";
 	}
	
	
	private String variableValuePath(String variableId){ 
		return variablePath(variableId) + "/value";
 	}
	
	private String variablePath(String variableId){
		return VARIABLES_PATH + "/" + variableId;
	}
}
