package net.conjur.api.exceptions.http;


/**
 * HttpException raised for 400 errors.
 */
@SuppressWarnings("serial")
public class MalformedRequestException extends HttpException {
	public MalformedRequestException() {
		super(400, "Malformed request");
	}
}
