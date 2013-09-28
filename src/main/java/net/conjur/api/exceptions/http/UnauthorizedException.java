package net.conjur.api.exceptions.http;

import org.apache.http.HttpStatus;

/**
 * HttpException raised for 401 errors.
 */
@SuppressWarnings("serial")
public class UnauthorizedException extends HttpException {
    static final int STATUS_CODE = HttpStatus.SC_UNAUTHORIZED;

	public UnauthorizedException() {
		super(STATUS_CODE);
	}

    public UnauthorizedException(Throwable cause){
        super(STATUS_CODE, cause);
    }
}
