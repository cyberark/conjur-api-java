package net.conjur.api.clients;

import net.conjur.api.AuthnProvider;
import net.conjur.api.Endpoints;
import net.conjur.api.Token;
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
public class AuthnClient extends ConjurClient implements AuthnProvider {

    private WebTarget login;
    private WebTarget authenticate;

	public AuthnClient(final String username, final String password, final Endpoints endpoints) {
	    super(username, password, endpoints);

        init(username);
	}

    public Token authenticate(String apiKey) {
	    Response res = authenticate.request("application/json").post(Entity.text(apiKey), Response.class);
	    validateResponse(res);

        return Token.fromJson(res.readEntity(String.class));
     }

    // implementation of AuthnProvider method
    public Token authenticate(String apiKey, boolean useCachedToken) {
        return authenticate(apiKey);
    }

    public String login(){
	    Response res = login.request("text/plain").get(Response.class);
	    validateResponse(res);

        return res.readEntity(String.class);
     }

    private void init(final String username){
        WebTarget root = getClient().target(getEndpoints().getAuthnUri());
        login = root.path("login");
        authenticate = root.path(encodeUriComponent(username)).path("authenticate");
    }

}
