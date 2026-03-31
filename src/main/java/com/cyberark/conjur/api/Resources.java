package com.cyberark.conjur.api;

import com.cyberark.conjur.api.clients.ResourceClient;

import javax.net.ssl.SSLContext;
import java.util.List;

/**
 * Facade for Conjur resource discovery operations (listing and counting resources).
 *
 * <p>This class is the counterpart to {@link Variables} and is accessible via
 * {@link Conjur}.resources(). It separates resource-management concerns from
 * secret/variable operations.</p>
 */
public class Resources {

    private final ResourceClient resourceClient;

    Resources(ResourceClient resourceClient) {
        this.resourceClient = resourceClient;
    }

    /**
     * List resources using the provided query parameters.
     *
     * @param query resource query parameters; use {@link ResourceQuery#all()} for no filters
     * @return resources matching the query
     */
    public List<ConjurResource> listResources(ResourceQuery query) {
        return resourceClient.listResources(query);
    }

    /**
     * Count resources using the provided query parameters.
     *
     * @param query resource query parameters; use {@link ResourceQuery#all()} for no filters
     * @return the number of matching resources
     */
    public int countResources(ResourceQuery query) {
        return resourceClient.countResources(query);
    }
}
