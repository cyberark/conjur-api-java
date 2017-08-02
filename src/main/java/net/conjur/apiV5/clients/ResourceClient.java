package net.conjur.apiV5.clients;

import net.conjur.apiV5.Endpoints;
import net.conjur.apiV5.Token;
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

    public String getVariable(String variableKey) {
        return variableRequest(variableKey).get(String.class);
    }

    public void setVariable(String variableKey, String variableValue) {
        variableRequest(variableKey).post(Entity.text(variableValue), String.class);
    }

    private Invocation.Builder variableRequest(String variableKey) {
        return secrets.path(variableKey)
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
