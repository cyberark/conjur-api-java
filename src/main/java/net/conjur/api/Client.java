package net.conjur.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import net.conjur.api.exceptions.ConjurApiException;
import net.conjur.api.exceptions.http.HttpException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Abstract base class for Conjur service clients.
 */
public abstract class Client {
	
	private final URI endpoint;
	
	/**
	 * Create a service from an endpoint URI.
	 * @param endpoint
	 */
	public Client(URI endpoint){
		this.endpoint = endpoint;
	}
	
	/**
	 * Create a service from an endpoint URI String.
	 * @param endpoint
	 */
	public Client(String endpoint){
		this(URI.create(endpoint));
	}
	
	/** 
	 * Create a client from the given endpoint configuration.
	 * @param endpoints the configuration
	 */
	public Client(Endpoints endpoints){
		this.endpoint = URI.create(getEndpointFromEndpoints(endpoints));
	}
	
	/**
	 * @return The base uri for the service used by this client
	 */
	public URI getEndpoint(){
		return this.endpoint;
	}
	
	/**
	 * Subclasses must implement this (poorly named) method to select 
	 * an endpoint from the given configuration.
	 * 
	 * @param endpoints the endpoint configuration to use
	 * @return the endpoint URI as a String
	 */
	protected abstract String getEndpointFromEndpoints(Endpoints endpoints);
	
	/**
	 * <p>Helper to execute the given request and return the response body as a 
	 * String.  Returning a String is the sensible thing to do here because
	 * Conjur service responses are small and returning a String allows this
	 * method to hide details such as error handling and connection management.</p>
	 * 
	 * <p>When the response body is null (for example, if an error occurs)
	 * an empty String is returned</p>
	 * 
	 * <p>All exceptions are wrapped in a ConjurApiException, with specific HTTP errors
	 * thrown as HttpException subclasses.  The base ConjurApiException class is reserved
	 * for low level network errors.</p>
	 * 
	 * @param request The HTTP request to execute.
	 * @return the response body as a String or an empty String if the response has no body.
	 * @throws ConjurApiException when any error occurs
	 * @throws HttpException when the HTTP response has an error status code
	 */
	protected String execute(HttpUriRequest request){
		HttpClient client = HttpClients.createDefault();
		try{
			try{
				HttpResponse response = client.execute(request);
				try{
					HttpException.throwErrors(response);
					HttpEntity entity = response.getEntity();
					return entity == null ? "" : EntityUtils.toString(response.getEntity());
				}finally{
					maybeClose(response);
				}
			}finally{
				maybeClose(client);
			}
		}catch(IOException e){
			throw new ConjurApiException(e);
		}
	}
	
	/**
	 * Helper to create an HttpUriRequest.  Subclasses will typically control the behavior of
	 * this method by overriding {@link #requestBuilder(String, String)} rather than this method. 
	 * @param method The HTTP method, such as <code>"GET"</code> or <code>"PUT"</code>.  
	 * 	If <code>null</code>, defaults to <code>"GET"</code>.
	 * @param path The request path.  If <code>null</code>, defaults to <code>"/"</code>.  Note that this must
	 * 	start with a <code>"/"</code> character.
	 * @return an {@link HttpUriRequest} configured with the given method and path.
	 */
	protected HttpUriRequest request(String method, String path){
		return requestBuilder(method, path).build();
	}
	
	/**
	 * Create a {@link RequestBuilder} for <code>"GET"</code> requests to <code>"/"</code>
	 * @return
	 */
	protected final RequestBuilder requestBuilder(){
		return requestBuilder(null);
	}
	
	/**
	 * Create a {@link RequestBuilder} for requests by <code>method</code> to <code>"/"</code>.
	 * @param method The HTTP method, such as <code>"GET"</code> or <code>"PUT"</code>.  Defaults
	 * 	to <code>"GET"</code> when <code>null</code>.
	 * @return a {@link RequestBuilder}
	 */
	protected final RequestBuilder requestBuilder(String method){
		return requestBuilder(method, null);
	}
	
	/**
	 * Helper to create an {@link RequestBuilder}.  Subclasses may override this to add common headers
	 * or other request configuration.  Overriding methods will typically update the value returned
	 * by the superclass method.
	 * 
	 * @param method The HTTP method, such as <code>"GET"</code> or <code>"PUT"</code>.  
	 * 	If <code>null</code>, defaults to <code>"GET"</code>.
	 * @param path The request path.  If <code>null</code>, defaults to <code>"/"</code>.  Note that this must
	 * 	start with a <code>"/"</code> character.
	 * @return an {@link RequestBuilder} configured with the given method and path.
	 */
	protected RequestBuilder requestBuilder(String method, String path){
		if(path == null)
			path = "/";
		
		if(method == null)
			method = "GET";
		
		if(!path.startsWith("/"))
			path = "/" + path;
		
		if(path.endsWith("/") && path.length() > 1)
			path = path.substring(0, path.length() - 1);
		
		method = method.toUpperCase();
		
		RequestBuilder builder =  RequestBuilder.create(method).setUri(requestUri(path));
		
		if("PUT".equals(method) || "POST".equals(method)){
			// HttpClient sets a funny charset, not sure what the right way to fix it
			// is.
			builder.removeHeaders("Content-Type")
				   .addHeader("Content-Type", "application/x-www-form-urlencoded");
		}
		
		return builder;
	}
	
	/**
	 * @return the {@link URI} for a request to <code>"/"</code>
	 */
	protected URI requestUri(){ 
		return requestUri(null);
	}
	
	/**
	 * Construct a {@link URI} for a request to <code>path</code> based on the {@link URI}
	 * returned by {@link #getEndpoint()}
	 * 
	 * @param path The path to append to our base URI.  If <code>null</code>, defaults to 
	 * <code>"/"</code>. 
	 * @return The {@link URI} for this path.
	 */
	protected URI requestUri(String path){
		if(path == null)
			return getEndpoint();
		try {
			return new URIBuilder(getEndpoint()).setPath(path).build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e); // this will more or less never happen, I think.
		}
	}
	
	/**
	 * Helper to close a {@link Closeable} object.  Used to close HttpResponses and HttpClients so
	 * that we don't work with non-interface CloseableXXX types.
	 * 
	 * @param maybeClosable An object that will have it's {@link Closeable#close()} method invoked
	 * if it happens to be an instanceof {@link Closeable}
	 * @throws IOException
	 */
	private void maybeClose(Object maybeClosable) throws IOException {
		if(maybeClosable instanceof Closeable){
			((Closeable)maybeClosable).close();
		}
	}
	
	/**
	 * Escape a URI component.
	 * @param uriComponent The String to escape.
	 * @return An escaped String
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
			// Never happens!
			throw new RuntimeException("WTF? UTF-8 encoding is apparently unsupported??", e);
		}
	}
}
