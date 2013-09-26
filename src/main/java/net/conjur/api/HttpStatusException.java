package net.conjur.api;

import org.apache.http.HttpResponse;

public class HttpStatusException extends Exception {
	private static final long serialVersionUID = -6383794072715460307L;
	private int statusCode;
	private String reasonPhrase;
	
	public int getStatusCode() {
		return statusCode;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	private String message;
	
	public HttpStatusException(int statusCode, String reasonPhrase){
		this.statusCode = statusCode;
		this.reasonPhrase = reasonPhrase;
	}
	
	
	public static HttpStatusException fromResponse(HttpResponse response){
		return new HttpStatusException(response.getStatusLine().getStatusCode(), 
				response.getStatusLine().getReasonPhrase());
	}
	
	public static void throwErrors(HttpResponse response) throws HttpStatusException{
		if(response.getStatusLine().getStatusCode() >= 400){
			throw fromResponse(response);
		}
	}
	
	@Override
	public String getMessage() {
		if(message == null){
			message = formatMessage(getStatusCode(), getReasonPhrase());
		}
		return message;
	}
	
	private static String formatMessage(int statusCode, String reasonPhrase){
		return String.format("HTTP request failed (%d %s)", statusCode, reasonPhrase);
	}
	
}
