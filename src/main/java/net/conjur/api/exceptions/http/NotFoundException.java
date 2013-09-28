package net.conjur.api.exceptions.http;


import org.apache.http.HttpStatus;

/**
 * HttpException raised for 404 errors.
 */
@SuppressWarnings("serial")
public class NotFoundException extends HttpException {
    public static final int STATUS_CODE = HttpStatus.SC_NOT_FOUND;


    public NotFoundException() {
		super(STATUS_CODE);
	}

    public NotFoundException(Throwable cause){
        super(STATUS_CODE, cause);
    }
}
