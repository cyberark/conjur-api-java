package net.conjur.api.authn;

import net.conjur.api.Client;

public class Authn extends Client {

	/**
	 * Exchange a username/apiKey pair for a token that can be used to authenticate
	 * future API calls.
	 * @param username a conjur username
	 * @param apiKey an api key as returned by {@link #authenticate(String, String)}
	 * @return a {@link Token} that can be used to authenticate api calls.
	 */
	public Token authenticate(String username, String apiKey){
		
	}
	
	/**
	 * Exchange a Conjur username and password for an API key
	 * @param username the conjur username
	 * @param password the password for the given username
	 * @return an API key
	 * @throws TODO what does this throw?
	 */
	public String login(String username, String password){
		
	}
	
	@Override
	protected String getEndpoint() {
		// TODO Auto-generated method stub
		return null;
	}

}
