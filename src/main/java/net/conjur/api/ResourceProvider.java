package net.conjur.api;

/**
 * Provides methods for retrieving and setting Conjur resources
 */
public interface ResourceProvider {

    /***
     * Fetch the value of a secret in the specified variable
     * @param variableId - id of the variable
     * @return The value of a secret from the specified variable
     */
    String retrieveSecret(String variableId);

    /**
     * Creates a secret value within the specified variable
     * @param variableId - id of the variable
     * @param secret - Secret value within the specified variable
     */
    void addSecret(String variableId, String secret);

}
