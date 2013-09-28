package net.conjur.api.authn;

import java.net.URI;

import net.conjur.api.Client;
import net.conjur.api.Endpoints;
import net.conjur.util.HttpHelpers;

import org.apache.http.Header;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;

/**
 * Conjur authentication service client.
 * 
 * This client provides methods to get API tokens from the conjur authentication service,
 * which can then be used to make authenticated calls to other conjur services.
 *
 */
public class AuthnClient extends Client {
    private URI uri;

	public AuthnClient(Endpoints endpoints) {
		super(endpoints);
	}

	/**
	 * Exchange a username/apiKey pair for a token that can be used to authenticate
	 * future API calls.
	 * @param username a conjur username
	 * @param apiKey an api key as returned by {@link #login(String, String)}
	 * @return a Token that can be used to create authenticated clients
	 */
	public Token authenticate(String username, String apiKey) {
        // POST users/<username>/authenticate with apiKey in body

        Request request = Request.Post(getUri("users", username, "authenticate"))
                .bodyString(apiKey, ContentType.TEXT_PLAIN);
        return responseJson(request, Token.class);
	}


	
	/**
	 * Exchange a Conjur username and password for an API key.
	 * 
	 * @param username the conjur username
	 * @param password the password for the given username
	 * @return an API key
	 */
	public String login(String username, String password){
        // GET users/login with basic auth
        return responseString(Request.Get(getUri("users/login"))
                .addHeader(HttpHelpers.basicAuthHeader(username, password)));
	}

    /**
     * Change a user's password
     * @param username the user whose password we want to change
     * @param oldPass the user's current password or api key
     * @param newPass the user's new password
     */
    public void updatePassword(String username, String oldPass, String newPass){
        response(Request.Put("users/password")
                .addHeader(HttpHelpers.basicAuthHeader(username,oldPass))
                .bodyString(newPass, ContentType.TEXT_PLAIN)
        );
    }


    @Override
    public URI getUri() {
        if(uri == null)
            uri = getEndpoints().authnUri();
        return uri;
    }
}
