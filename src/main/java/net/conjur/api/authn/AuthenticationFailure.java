package net.conjur.api.authn;

import net.conjur.api.ConjurApiException;

public class AuthenticationFailure extends ConjurApiException {
	private static final long serialVersionUID = -5568546899895466039L;

	public AuthenticationFailure() {
		this("Authentication failed");
	}

	public AuthenticationFailure(String msg) {
		super(msg);
	}

	public AuthenticationFailure(Throwable cause) {
		this("Authentication failed", cause);
	}

	public AuthenticationFailure(String msg, Throwable cause) {
		super(msg, cause);
	}

}
