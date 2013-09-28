package net.conjur.api.exceptions.http;


import org.apache.http.HttpStatus;

/**
 * HttpException raised for 400 errors.
 */
@SuppressWarnings("serial")
public class BadRequestException extends HttpException {
    public static final int STATUS_CODE = HttpStatus.SC_BAD_REQUEST;

	public BadRequestException() {
		super(STATUS_CODE);
	}

    public BadRequestException(Throwable cause){
        super(STATUS_CODE, cause);
    }
}
