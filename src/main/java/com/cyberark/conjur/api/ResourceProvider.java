package com.cyberark.conjur.api;

import java.util.Map;

/**
 * Provides methods for retrieving and setting Conjur resources
 */
public interface ResourceProvider {

    /**
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

    /**
     * Fetch multiple secret values in one invocation.
     * It's faster to fetch secrets in batches than to fetch them one at a time.
     *
     * @param variableIds the variable IDs to retrieve (without account prefix)
     * @return a map of variable ID to secret value
     * @see <a href="https://docs.cyberark.com/conjur-open-source/latest/en/content/developer/conjur_api_batch_retrieve.htm">Batch Secret Retrieval</a>
     */
    default Map<String, String> retrieveBatchSecrets(String... variableIds) {
        throw new UnsupportedOperationException("Batch secret retrieval not supported");
    }

}
