package net.conjur.api.exceptions.http;

import org.apache.http.HttpStatus;

/**
 * HttpException raised for 401 errors.
 */
@SuppressWarnings("serial")
public class UnauthorizedException extends HttpException {
	public UnauthorizedException() {
		super(HttpStatus.SC_UNAUTHORIZED);
	}

    public UnauthorizedException
}
