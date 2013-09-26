package net.conjur.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public abstract class Client {

	protected abstract String getEndpoint();
	
	/**
	 * Return a URI for our endpoint.
	 * @return a URI for our endpoint
	 */
	protected URI getUri(){
		return URI.create(getEndpoint());
	}
	
	protected HttpResponse response(HttpUriRequest request) throws IOException{
		return response(createHttpClient(), request);
	}
	
	protected HttpResponse response(HttpClient client, HttpUriRequest request) throws IOException{
		try{
			HttpResponse response = client.execute(request);
			HttpStatusException.throwErrors(response);
			return response;		
		}finally{
			if(client instanceof CloseableHttpClient){
				((CloseableHttpClient)client).close();
			}
		}
	}
	
	protected JsonElement responseJson(HttpClient client, HttpUriRequest request) throws IOException {
		return new JsonParser().parse(responseString(client, request));
	}
	
	protected JsonElement responseJson(HttpUriRequest request) throws IOException {
		return responseJson(createHttpClient(), request);
	}
	
	protected String responseString(HttpClient client, HttpUriRequest request) throws IOException {
		try{
			HttpResponse response =  response(client, request);
			try{
				HttpStatusException.throwErrors(response);
				return response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity());
			}finally{
				maybeClose(response);
			}
		}finally{
			maybeClose(client);
		}
	}
	
	protected String responseString(HttpUriRequest request) throws IOException{
		return responseString(createHttpClient(), request);
	}
	
	private void maybeClose(Object maybeClosable) throws IOException {
		if(maybeClosable instanceof Closeable){
			((Closeable)maybeClosable).close();
		}
	}

	

	protected HttpClient createHttpClient(CredentialsProvider credentialsProvider){
		return HttpClients.custom()
				.setDefaultCredentialsProvider(credentialsProvider)
				.build();
	}
	
	protected HttpClient createHttpClient(String username){
		return createHttpClient(credentialsFor(username));
	}
	
	protected HttpClient createHttpClient(String username, String password){
		return createHttpClient(credentialsFor(username,password));
	}
	
	protected HttpClient createHttpClient(){
		return HttpClients.createDefault();
	}
	
	protected HttpUriRequest createRequest(String method, String path){
		return createRequestBuilder(method, path).build();
	}
	
	protected RequestBuilder createRequestBuilder(String method, String path){
		return RequestBuilder.create(method).setUri(getUriWithPath(path));
	}
	
	/**
	 * Create a {@link CredentialsProvider} for basic auth 
	 * with the given username and password.
	 * @param username basic auth username
	 * @param password basic auth password
	 * @return a {@link CredentialsProvider} configured to authenticate as the given identity.
	 */
	protected CredentialsProvider credentialsFor(String username, String password){
		URI uri = getUri();
		CredentialsProvider creds = new BasicCredentialsProvider();
		creds.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), 
				new UsernamePasswordCredentials(username, password));
		return creds;
	}
	
	protected CredentialsProvider credentialsFor(String username){
		return credentialsFor(username, null);
	}
	
	protected URI getUriWithPath(String path){
		URIBuilder builder = new URIBuilder(getUri());
		if(path != null){
			builder.setPath(path);
		}
		try {
			return builder.build();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Should not happen!", e);
		}
	}
	
	/**
	 * Encode a URL component.
	 * @param uriComponent
	 * @return
	 */
	public static String fullyEscape(String uriComponent){
		try {
			// SO...Lets discuss this!
			// api-ruby uses CGI.escape, and the JRuby implementation of that method uses URLEncoder, 
			// so I think this is good.  However, the api-node implementation uses encodeURIComponent,
			// which is *not* exactly the same!  See this stack overflow question: http://tinyurl.com/nmbu4ge.
			// Not sure what to do about this.
			return URLEncoder.encode(uriComponent, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("WTF? UTF-8 encoding is apparently unsupported??", e);
		}
	}
}
