package net.conjur.api;

import net.conjur.api.clients.ResourceClient;

/**
 * Entry point for the Conjur API client.
 */
public class Conjur {

    private static Conjur instance = new Conjur();

    private static ResourceClient resourceClient;

    private Conjur(){
        Credentials credentials = Credentials.fromSystemProperties();

        resourceClient = new ResourceClient(
                credentials.getUsername(), credentials.getPassword(), Endpoints.fromSystemProperties());
    }

    /**
     * @return an instance of the singleton object
     */
    public static Conjur getInstance() {
        return instance;
    }

    public String retrieveSecret(String variableId) {
        return resourceClient.retrieveSecret(variableId);
    }

    public void addSecret(String variableId, String secret){
        resourceClient.addSecret(variableId, secret);
    }
}
