package com.cyberark.conjur.api.clients;

import java.io.IOException;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cyberark.conjur.api.Configuration;

public class TelemetryHeaderTest {

    @Test
public void testTelemetryHeaderAdded() throws IOException {
    ClientRequestContext requestContext = mock(ClientRequestContext.class);
    MultivaluedMap<String, Object> headers = new jakarta.ws.rs.core.MultivaluedHashMap<>();
    
    when(requestContext.getHeaders()).thenReturn(headers);

    Configuration config = new Configuration();
    TelemetryHeaderFilter telemetryFilter = new TelemetryHeaderFilter(config);

    telemetryFilter.filter(requestContext);

    assertTrue(headers.containsKey("x-cybr-telemetry"), "Telemetry header should be added.");
    
}
}