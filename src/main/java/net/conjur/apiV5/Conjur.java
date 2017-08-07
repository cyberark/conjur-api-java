package net.conjur.apiV5;

import net.conjur.apiV5.clients.AuthnClient;
import net.conjur.apiV5.clients.ResourceClient;

/**
 * Entry point for the Conjur API client.
 */
public class Conjur {

    private static Conjur instance = new Conjur();

    private static ResourceClient resourceClient;
    private static AuthnClient authnClient;

    private Conjur(){
        Credentials credentials = Credentials.fromSystemProperties();

        authnClient = new AuthnClient(credentials.getUsername(), credentials.getPassword(), Endpoints.fromSystemProperties());
        resourceClient = new ResourceClient(credentials.getUsername(), credentials.getPassword(), Endpoints.fromSystemProperties());

        // TODO orenbm: validate URL? Do we have a simple request for this? not sure that login is the right one for this
    }

    /**
     * @return an instance of the singleton object
     */
    public static Conjur getInstance() {
        if (!isTokenValid()) {
            getAccessToken();
        }

        return instance;
    }

    private static void getAccessToken() {
        String apiKey = authnClient.login();

        Token token = authnClient.authenticate(apiKey);

        resourceClient.setToken(token);
    }

    private static boolean isTokenValid() {
        Token token = resourceClient.getToken();
        return token != null && !token.isExpired();
    }

    public String retrieveSecret(String variableId) {
        return resourceClient.retrieveSecret(variableId);
    }

    public void addSecret(String variableId, String secret){
        resourceClient.addSecret(variableId, secret);
    }
}
