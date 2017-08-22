package net.conjur.api.clients;

import net.conjur.api.AuthnProvider;
import net.conjur.api.Endpoints;
import net.conjur.api.Token;
import net.conjur.util.rs.HttpBasicAuthFilter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static net.conjur.util.EncodeUriComponent.encodeUriComponent;

/**
 * Conjur authentication service client.
 * 
 * This client provides methods to get API tokens from the conjur authentication service,
 * which can then be used to make authenticated calls to other conjur services.
 *
 */
public class AuthnClient implements AuthnProvider {

    private WebTarget login;
    private WebTarget authenticate;

    private final Endpoints endpoints;

    private String apiKey;

	public AuthnClient(final String username, final String password, final Endpoints endpoints) {
        this.endpoints = endpoints;

        init(username, password);

        // replacing the password with an API key
        this.apiKey = login();
    }

    public Token authenticate() {
	    Response res = authenticate.request("application/json").post(Entity.text(apiKey), Response.class);
	    validateResponse(res);

        return Token.fromJson(res.readEntity(String.class));
     }

    // implementation of AuthnProvider method
    public Token authenticate(boolean useCachedToken) {
        return authenticate();
    }

    /**
     * Login to a Conjur account with the credentials specified in the configuration
     * @return The API key of the user
     */
    public String login(){
	    Response res = login.request("text/plain").get(Response.class);
	    validateResponse(res);

        return res.readEntity(String.class);
     }

    private void init(final String username, final String password){
        final ClientBuilder builder = ClientBuilder.newBuilder()
                .register(new HttpBasicAuthFilter(username, password));

        Client client = builder.build();
        WebTarget root = client.target(endpoints.getAuthnUri());

        login = root.path("login");
        authenticate = root.path(encodeUriComponent(username)).path("authenticate");
    }

    // TODO orenbm: Remove when we have a response filter to handle this
    private void validateResponse(Response response) {
        int status = response.getStatus();
        if (status < 200 || status >= 400) {
            String errorMessage = String.format("Error code: %d, Error message: %s", status, response.readEntity(String.class));
            throw new WebApplicationException(errorMessage, status);
        }
    }

}
