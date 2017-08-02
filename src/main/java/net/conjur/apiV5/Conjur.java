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

    /***
     * Fetch the value of a secret in the specified variable
     * @param variableKey - id of the variable
     * @return The value of the variable
     */
    public String getVariable(String variableKey) {
        return resourceClient.getVariable(variableKey);
    }

    /**
     * Creates a secret value within the specified variable
     * @param variableKey - id of the variable
     * @param variableValue - new value of the variable
     */
    public void setVariable(String variableKey, String variableValue){
        resourceClient.setVariable(variableKey, variableValue);
    }
}
