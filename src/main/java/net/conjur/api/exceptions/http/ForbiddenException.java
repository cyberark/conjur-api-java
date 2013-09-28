package net.conjur.api.exceptions.http;

import org.apache.http.HttpStatus;

/**
 * HttpException raised for 403 errors.
 */
@SuppressWarnings("serial")
public class ForbiddenException extends HttpException {
    public static final int STATUS_CODE = HttpStatus.SC_FORBIDDEN;


    public ForbiddenException(){
		super(STATUS_CODE);
	}

    public ForbiddenException(Throwable cause){
        super(STATUS_CODE, cause);
    }
}
