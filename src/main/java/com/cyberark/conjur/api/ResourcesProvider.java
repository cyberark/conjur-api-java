package com.cyberark.conjur.api;

import java.util.List;

/**
 * Provides methods for interacting with Conjur resources.
 *
 * <p>This interface covers operations on the Conjur resources API, such as listing
 * and counting resources by kind or search term. It is intentionally separate from
 * the {@link ResourceProvider} interface, which is scoped to variable/secret
 * operations.</p>
 */
public interface ResourcesProvider {
    /**
     * List resources using the provided query parameters.
     *
     * @param query resource query parameters
     * @return resources matching the query
     * @see <a href="https://docs.cyberark.com/conjur-open-source/latest/en/content/developer/conjur_api_list_resources.htm">List Resources</a>
     */
    default List<ConjurResource> listResources(ResourceQuery query) {
        throw new UnsupportedOperationException("List resources not supported");
    }
    /**
     * Count resources using the provided query parameters.
     *
     * @param query resource query parameters
     * @return the number of matching resources
     * @see <a href="https://docs.cyberark.com/conjur-open-source/latest/en/content/developer/conjur_api_list_resources.htm">List Resources</a>
     */
    default int countResources(ResourceQuery query) {
        throw new UnsupportedOperationException("Count resources not supported");
    }
}
