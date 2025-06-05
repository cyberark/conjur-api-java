package com.cyberark.conjur.api.clients;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;

import com.cyberark.conjur.api.Configuration;

public class TelemetryHeaderFilter implements ClientRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(TelemetryHeaderFilter.class.getName());

    private final Configuration config;

    /**
     * Constructs a new TelemetryHeaderFilter with the specified configuration.
     * 
     * @param config the Configuration object used to retrieve the telemetry header
     */
    public TelemetryHeaderFilter(Configuration config) {
        this.config = config;
    }

    /**
     * Filters the client request and adds the telemetry header.
     * 
     * @param requestContext the request context to be modified
     * @throws IOException if an error occurs while modifying the request
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        // Get the telemetry header from the Configuration class
        String telemetryHeaderValue = config.getTelemetryHeader();
        
        // Add the telemetry header to the request
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add("x-cybr-telemetry", telemetryHeaderValue);
    }
}