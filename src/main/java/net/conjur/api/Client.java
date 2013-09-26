package net.conjur.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public abstract class Client {
	private CloseableHttpClient httpClient;
	
	protected abstract String getEndpoint();
	
	/**
	 * Return a URI for our endpoint.
	 * @return a URI for our endpoint
	 */
	protected URI getURI(){
		return URI.create(getEndpoint());
	}
	
	
	protected String requestString(CloseableHttpClient client, HttpUriRequest request) throws IOException, HttpStatusException {
		try{
			CloseableHttpResponse response = client.execute(request);
			try{
				HttpStatusException.throwErrors(response);
				if(response.getEntity() == null)
					return "";
				return EntityUtils.toString(response.getEntity());
			}finally{
				response.close();
			}
		}finally{
			client.close();
		}
	}
	
	/**
	 * Create an HttpClient configured to use basic auth
	 * @param username Basic auth username
	 * @param password Basic auth password
	 * @return An HttpClient instance that will authenticate as the given username/password.
	 */
	protected CloseableHttpClient httpClientWithBasicAuth(String username, String password){
		return HttpClients.custom()
				.setDefaultCredentialsProvider(credentialsFor(username, password))
				.build();
	}
	
	/**
	 * Return a (cached) default {@link CloseableHttpClient}
	 */
	protected CloseableHttpClient defaultHttpClient(){
		if(httpClient == null){
			httpClient = HttpClients.createDefault();
		}
		return  httpClient;
	}
	
	/**
	 * Create a {@link CredentialsProvider} for basic auth 
	 * with the given username and password.
	 * @param username basic auth username
	 * @param password basic auth password
	 * @return a {@link CredentialsProvider} configured to authenticate as the given identity.
	 */
	protected CredentialsProvider credentialsFor(String username, String password){
		URI uri = getURI();
		CredentialsProvider creds = new BasicCredentialsProvider();
		creds.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), 
				new UsernamePasswordCredentials(username, password));
		return creds;
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
