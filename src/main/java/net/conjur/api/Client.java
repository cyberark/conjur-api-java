package net.conjur.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public abstract class Client {
	
	private final URI endpoint;
	
	public Client(URI endpoint){
		this.endpoint = endpoint;
	}
	
	public Client(String endpoint){
		this(URI.create(endpoint));
	}
	
	/**
	 * @return The base uri for this client
	 */
	public URI getEndpoint(){
		return this.endpoint;
	}
	
	protected String execute(HttpUriRequest request) throws IOException{
		HttpClient client = HttpClients.createDefault();
		try{
			HttpResponse response = client.execute(request);
			try{
				HttpStatusException.throwErrors(response);
				HttpEntity entity = response.getEntity();
				return entity == null ? "" : EntityUtils.toString(response.getEntity());
			}finally{
				maybeClose(response);
			}
		}finally{
			maybeClose(client);
		}
	}
	
	protected HttpUriRequest request(String method, String path){
		return requestBuilder(method, path).build();
	}
	
	protected final RequestBuilder requestBuilder(){
		return requestBuilder(null);
	}
	
	protected final RequestBuilder requestBuilder(String method){
		return requestBuilder(method, null);
	}
	
	protected RequestBuilder requestBuilder(String method, String path){
		if(method == null)
			method = "GET";
		
		method = method.toUpperCase();
		
		return RequestBuilder.create(method).setUri(requestUri(path))
				// default sets a funny charset
				.addHeader("Content-Type", "application/x-www-form-urlencoded");
	}
	
	
	protected URI requestUri(){ 
		return requestUri(null);
	}
	
	protected URI requestUri(String path){
		if(path == null)
			return getEndpoint();
		try {
			return new URIBuilder(getEndpoint()).setPath(path).build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e); // this will more or less never happen, I think.
		}
	}
	
	private void maybeClose(Object maybeClosable) throws IOException {
		if(maybeClosable instanceof Closeable){
			((Closeable)maybeClosable).close();
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
