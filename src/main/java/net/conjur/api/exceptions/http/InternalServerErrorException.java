package net.conjur.api.exceptions.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;


/**
 * HttpException raised for 5xx errors.
 */
@SuppressWarnings("serial")
public class InternalServerErrorException extends HttpException {
    private static final int STATUS_CODE = HttpStatus.SC_INTERNAL_SERVER_ERROR;

	public InternalServerErrorException() {
		super(STATUS_CODE);
	}

	public InternalServerErrorException(Throwable e){
		super(STATUS_CODE, e);
	}
}
