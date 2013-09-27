package net.conjur.api.exceptions.http;

/**
 * HttpException raised for 403 errors.
 */
@SuppressWarnings("serial")
public class ForbiddenException extends HttpException {
	public ForbiddenException(){
		super(403, "Forbidden");
	}
}
