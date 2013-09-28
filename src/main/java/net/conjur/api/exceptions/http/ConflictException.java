package net.conjur.api.exceptions.http;

import org.apache.http.HttpStatus;

/**
 *
 */
public class ConflictException extends HttpException {
    public static final int STATUS_CODE = HttpStatus.SC_CONFLICT;


    public ConflictException() {
        super(STATUS_CODE);
    }

    public ConflictException(Throwable cause) {
        super(STATUS_CODE, cause);
    }
}
