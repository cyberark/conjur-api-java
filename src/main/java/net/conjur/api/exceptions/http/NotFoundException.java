package net.conjur.api.exceptions.http;


/**
 * HttpException raised for 404 errors.
 */
@SuppressWarnings("serial")
public class NotFoundException extends HttpException {
	public NotFoundException() {
		super(404, "Not found");
	}
}
