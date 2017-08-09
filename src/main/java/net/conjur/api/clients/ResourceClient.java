package net.conjur.api.clients;

import net.conjur.api.Endpoints;
import net.conjur.api.ResourceProvider;
import net.conjur.api.Token;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

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
        Response res =  buildVariableRequest(variableId).get(Response.class);
        validateResponse(res);

        return res.readEntity(String.class);
    }

    public void addSecret(String variableId, String secret) {
        Response res = buildVariableRequest(variableId).post(Entity.text(secret), Response.class);
        validateResponse(res);
    }

    private Invocation.Builder buildVariableRequest(String variableId) {
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
