package net.conjur.api.exceptions.http;


/**
 * HttpException raised for 400 errors.
 */
@SuppressWarnings("serial")
public class BadRequestException extends HttpException {
	public BadRequestException() {
		super(400, "Malformed request");
	}
}
