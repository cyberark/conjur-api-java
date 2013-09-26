/**
 * 
 */
package net.conjur.api;

/**
 * Exception thrown by the conjur api, typically when lower level components throw
 * checked exceptions.
 */
public class ConjurApiException extends Exception {
	private static final long serialVersionUID = 8235544304701005068L;

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
