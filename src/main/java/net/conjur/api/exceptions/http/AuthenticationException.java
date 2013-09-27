package net.conjur.api.exceptions.http;


/**
 * HttpException raised for 401 errors.
 */
@SuppressWarnings("serial")
public class AuthenticationException extends HttpException {
	public AuthenticationException() {
		super(401, "Authentication failed");
	}
}
