package net.conjur.api;

import net.conjur.api.clients.ResourceClient;

/**
 * Entry point for the Conjur API client.
 */
public class Conjur {

    private static ResourceClient resourceClient;

    public Conjur(String username, String password) {
        init(new Credentials(username, password));
    }

    /**
     * Default ctor creates a Conjur object with credentials from the system properties
     */
    public Conjur(){
        init(Credentials.fromSystemProperties());
    }

    private void init(Credentials credentials) {
        resourceClient = new ResourceClient(
                credentials.getUsername(), credentials.getPassword(), Endpoints.fromSystemProperties());
    }

    public String retrieveSecret(String variableId) {
        return resourceClient.retrieveSecret(variableId);
    }

    public void addSecret(String variableId, String secret){
        resourceClient.addSecret(variableId, secret);
    }
}
