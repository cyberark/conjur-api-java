package net.conjur.api;

import net.conjur.api.clients.ResourceClient;

public class Variables {

    private ResourceClient resourceClient;

    public Variables(Credentials credentials) {
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
