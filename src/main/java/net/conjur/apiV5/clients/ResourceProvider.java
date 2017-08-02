package net.conjur.apiV5.clients;

/**
 * Provides methods for retrieving and setting Conjur resources
 */
public interface ResourceProvider {

    /***
     * Fetch the value of a secret in the specified variable
     * @param variableKey - id of the variable
     * @return The value of the variable
     */
    String getVariable(String variableKey);

    /**
     * Creates a secret value within the specified variable
     * @param variableKey - id of the variable
     * @param variableValue - new value of the variable
     */
    void setVariable(String variableKey, String variableValue);

}
