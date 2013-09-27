package net.conjur.api.exceptions.http;

import org.apache.http.HttpResponse;


/**
 * HttpException raised for 5xx errors.
 */
@SuppressWarnings("serial")
public class ServerErrorException extends HttpException {
	public ServerErrorException() {
		super(500, "Malformed request");
	}
	public ServerErrorException(HttpResponse httpResponse){
		super(httpResponse);
	}
}
