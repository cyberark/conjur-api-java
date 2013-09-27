package net.conjur.api.exceptions.http;

import net.conjur.api.exceptions.ConjurApiException;

import org.apache.http.HttpResponse;

/**
 * Thrown when an HTTP request returns an error (>= 400) status.  Some specific error codes
 * (404, 401, etc.) throw exceptions found in the net.conjur.api.exceptions.http package,
 * which are all subclasses of this exception.
 */
@SuppressWarnings("serial")
public class HttpException extends ConjurApiException {
	
	private final int statusCode;
	private final String reasonPhrase;
	
	/**
	 * @return The HTTP status code that caused this exception to be raised.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @return The reason this status code was returned, taken from the HTTP response status 
	 * line.
	 */
	public String getReasonPhrase() {
		return reasonPhrase;
	}

	private String message;
	
	/**
	 * Creates an HttpException with the given status code and reason.
	 * @param statusCode the status code
	 * @param reasonPhrase the reason this status was returned
	 */
	public HttpException(int statusCode, String reasonPhrase){
		this.statusCode = statusCode;
		this.reasonPhrase = reasonPhrase;
	}
	
	/**
	 * Create an HttpException for the given HttpResponse.
	 * @param httpResponse response to take the status code and reason from
	 */
	public HttpException(HttpResponse httpResponse){
		this(httpResponse.getStatusLine().getStatusCode(), 
			 httpResponse.getStatusLine().getReasonPhrase());
	}
	
	
	public static HttpException fromResponse(HttpResponse response){
		return new HttpException(response.getStatusLine().getStatusCode(), 
				response.getStatusLine().getReasonPhrase());
	}
	
	public static void throwErrors(HttpResponse response) throws HttpException{
		int code = response.getStatusLine().getStatusCode();
		
		if(code < 400)
			return;
		
		switch(code){
		case 400:
			throw new MalformedRequestException();
		case 401:
			throw new AuthenticationException();
		case 403:
			throw new ForbiddenException();
		case 404:
			throw new NotFoundException();
		default:
			if(code >= 500)
				throw new ServerErrorException(response);
			throw new HttpException(response);
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
