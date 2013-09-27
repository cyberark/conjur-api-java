/**
 * 
 */
package net.conjur.api.exceptions;

/**
 * Exception thrown by the conjur api, typically when lower level components throw
 * checked exceptions.
 */
@SuppressWarnings("serial")
public class ConjurApiException extends RuntimeException {
	public ConjurApiException() {}

	public ConjurApiException(String msg) {
		super(msg);
	}

	public ConjurApiException(Throwable cause) {
		super(cause);
	}
	
	public ConjurApiException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
