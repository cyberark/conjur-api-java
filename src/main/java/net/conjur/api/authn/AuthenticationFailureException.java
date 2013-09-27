package net.conjur.api.authn;

import net.conjur.api.ConjurApiException;

public class AuthenticationFailureException extends ConjurApiException {
	private static final long serialVersionUID = -5568546899895466039L;

	public AuthenticationFailureException() {
		this("Authentication failed");
	}

	public AuthenticationFailureException(String msg) {
		super(msg);
	}

	public AuthenticationFailureException(Throwable cause) {
		this("Authentication failed", cause);
	}

	public AuthenticationFailureException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
