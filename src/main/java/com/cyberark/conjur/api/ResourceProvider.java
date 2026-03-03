package com.cyberark.conjur.api;

import java.util.List;
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

    /**
     * List resources visible to the authenticated identity.
     *
     * @return all resources
     * @see <a href="https://docs.cyberark.com/conjur-open-source/latest/en/content/developer/conjur_api_list_resources.htm">List Resources</a>
     */
    default List<ConjurResource> listResources() {
        throw new UnsupportedOperationException("List resources not supported");
    }

    /**
     * List resources filtered by kind.
     *
     * @param kind the resource kind to filter by (e.g. "variable", "host", "user", "group", "layer", "policy", "webservice")
     * @return resources matching the given kind
     */
    default List<ConjurResource> listResources(String kind) {
        throw new UnsupportedOperationException("List resources not supported");
    }

    /**
     * List resources with full query parameter control.
     *
     * @param kind   resource kind filter (null for all kinds)
     * @param search text search filter (null for no search)
     * @param limit  max results (null for server default)
     * @param offset pagination offset (null for no offset)
     * @return resources matching the query
     */
    default List<ConjurResource> listResources(String kind, String search, Integer limit, Integer offset) {
        throw new UnsupportedOperationException("List resources not supported");
    }

    /**
     * Count resources visible to the authenticated identity.
     *
     * @param kind   resource kind filter (null for all kinds)
     * @param search text search filter (null for no search)
     * @return the number of matching resources
     */
    default int countResources(String kind, String search) {
        throw new UnsupportedOperationException("Count resources not supported");
    }

}
