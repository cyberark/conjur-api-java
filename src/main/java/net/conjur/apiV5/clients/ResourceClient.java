package net.conjur.apiV5.clients;

import net.conjur.apiV5.Endpoints;
import net.conjur.apiV5.Token;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

/**
 * Conjur service client.
 */
public class ResourceClient extends ConjurClient implements ResourceProvider {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Token token=\"%s\"";

    private WebTarget secrets;
    private Token token;

	public ResourceClient(final String username, final String password, final Endpoints endpoints) {
	    super(username, password, endpoints);

        init();
	}

    public String retrieveSecret(String variableId) {
	    try {
            return variableRequest(variableId).get(String.class);
        } catch (NotFoundException e) {
	        throw new ConjurException(String.format("Variable '%s' not found in this account", variableId));
        }
    }

    public void addSecret(String variableId, String secret) {
	    try {
            variableRequest(variableId).post(Entity.text(secret), String.class);
        } catch (NotFoundException e) {
            throw new ConjurException(String.format("Variable '%s' not found in this account", variableId));
        }
    }

    private Invocation.Builder variableRequest(String variableId) {
        return secrets.path(variableId)
                .request()
                .header(AUTHORIZATION_HEADER, String.format(AUTHORIZATION_HEADER_VALUE, token.toBase64()));
    }

    private void init(){
        secrets = getClient().target(getEndpoints().getSecretsUri());
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

}
