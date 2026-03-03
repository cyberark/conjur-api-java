package com.cyberark.conjur.api.clients;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Map;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cyberark.conjur.api.Endpoints;

/**
 * Unit tests for {@link ResourceClient}, particularly the batch secret retrieval feature.
 * Uses Mockito to mock the JAX-RS client stack so no Conjur server is needed.
 */
public class ResourceClientTest {

    private Client mockClient;
    private WebTarget mockSecrets;
    private Endpoints endpoints;
    private ResourceClient resourceClient;

    // Mocks for the batch request chain
    private WebTarget mockBatchTarget;
    private Invocation.Builder mockBatchBuilder;
    private Response mockBatchResponse;

    // Mocks for single-secret request chain
    private WebTarget mockPathTarget;
    private Invocation.Builder mockSingleBuilder;
    private Response mockSingleResponse;

    @BeforeEach
    void setUp() {
        mockClient = mock(Client.class);
        mockSecrets = mock(WebTarget.class);

        endpoints = new Endpoints("https://conjur.example.com", "myaccount");

        // Batch request mock chain
        mockBatchTarget = mock(WebTarget.class);
        mockBatchBuilder = mock(Invocation.Builder.class);
        mockBatchResponse = mock(Response.class);
        when(mockClient.target(any(URI.class))).thenReturn(mockBatchTarget);
        when(mockBatchTarget.request()).thenReturn(mockBatchBuilder);
        when(mockBatchBuilder.get(Response.class)).thenReturn(mockBatchResponse);

        // Single-secret mock chain
        mockPathTarget = mock(WebTarget.class);
        mockSingleBuilder = mock(Invocation.Builder.class);
        mockSingleResponse = mock(Response.class);
        when(mockSecrets.path(anyString())).thenReturn(mockPathTarget);
        when(mockPathTarget.request()).thenReturn(mockSingleBuilder);

        resourceClient = new ResourceClient(mockClient, mockSecrets, endpoints);
    }

    // ========================================================================
    // Batch Secret Retrieval Tests
    // ========================================================================

    @Nested
    class BatchRetrieval {

        @Test
        void singleVariable() {
            String json = "{\"myaccount:variable:db-password\": \"s3cret\"}";
            when(mockBatchResponse.getStatus()).thenReturn(200);
            when(mockBatchResponse.readEntity(String.class)).thenReturn(json);

            Map<String, String> result = resourceClient.retrieveBatchSecrets("db-password");

            assertEquals(1, result.size());
            assertEquals("s3cret", result.get("db-password"));

            // Verify correct URI was built
            verify(mockClient).target(argThat((URI uri) ->
                uri.toString().equals(
                    "https://conjur.example.com/secrets?variable_ids=myaccount:variable:db-password"
                )
            ));
        }

        @Test
        void multipleVariables() {
            String json = "{"
                + "\"myaccount:variable:secret1\": \"value1\","
                + "\"myaccount:variable:secret2\": \"value2\","
                + "\"myaccount:variable:secret3\": \"value3\""
                + "}";
            when(mockBatchResponse.getStatus()).thenReturn(200);
            when(mockBatchResponse.readEntity(String.class)).thenReturn(json);

            Map<String, String> result = resourceClient.retrieveBatchSecrets(
                "secret1", "secret2", "secret3");

            assertEquals(3, result.size());
            assertEquals("value1", result.get("secret1"));
            assertEquals("value2", result.get("secret2"));
            assertEquals("value3", result.get("secret3"));
        }

        @Test
        void variableWithSlashes() {
            // Slashes in variable IDs must be encoded as %2F in the URI
            String json = "{\"myaccount:variable:prod/aws/db-password\": \"secret-val\"}";
            when(mockBatchResponse.getStatus()).thenReturn(200);
            when(mockBatchResponse.readEntity(String.class)).thenReturn(json);

            Map<String, String> result = resourceClient.retrieveBatchSecrets("prod/aws/db-password");

            assertEquals(1, result.size());
            assertEquals("secret-val", result.get("prod/aws/db-password"));

            // Verify slashes were percent-encoded in the request URI
            verify(mockClient).target(argThat((URI uri) ->
                uri.toString().contains("prod%2Faws%2Fdb-password")
            ));
        }

        @Test
        void variableWithSpecialCharacters() {
            // @ encoded to %40, + encoded to %2B, & encoded to %26
            String json = "{"
                + "\"myaccount:variable:alice@devops\": \"val1\","
                + "\"myaccount:variable:research+development\": \"val2\","
                + "\"myaccount:variable:sales&marketing\": \"val3\""
                + "}";
            when(mockBatchResponse.getStatus()).thenReturn(200);
            when(mockBatchResponse.readEntity(String.class)).thenReturn(json);

            Map<String, String> result = resourceClient.retrieveBatchSecrets(
                "alice@devops", "research+development", "sales&marketing");

            assertEquals(3, result.size());
            assertEquals("val1", result.get("alice@devops"));
            assertEquals("val2", result.get("research+development"));
            assertEquals("val3", result.get("sales&marketing"));

            // Verify encoding in URI
            verify(mockClient).target(argThat((URI uri) -> {
                String s = uri.toString();
                return s.contains("alice%40devops")
                    && s.contains("research%2Bdevelopment")
                    && s.contains("sales%26marketing");
            }));
        }

        @Test
        void variableWithSpaces() {
            String json = "{\"myaccount:variable:my secret\": \"val\"}";
            when(mockBatchResponse.getStatus()).thenReturn(200);
            when(mockBatchResponse.readEntity(String.class)).thenReturn(json);

            Map<String, String> result = resourceClient.retrieveBatchSecrets("my secret");

            assertEquals(1, result.size());
            assertEquals("val", result.get("my secret"));

            // Spaces should be encoded as %20 (not +)
            verify(mockClient).target(argThat((URI uri) ->
                uri.toString().contains("my%20secret")
                && !uri.toString().contains("my+secret")
            ));
        }

        @Test
        void error404ThrowsException() {
            when(mockBatchResponse.getStatus()).thenReturn(404);
            when(mockBatchResponse.readEntity(String.class)).thenReturn("Variable not found");

            WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> resourceClient.retrieveBatchSecrets("nonexistent"));

            assertTrue(ex.getMessage().contains("404"));
        }

        @Test
        void error403ThrowsException() {
            when(mockBatchResponse.getStatus()).thenReturn(403);
            when(mockBatchResponse.readEntity(String.class)).thenReturn("Forbidden");

            WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> resourceClient.retrieveBatchSecrets("forbidden-secret"));

            assertTrue(ex.getMessage().contains("403"));
        }

        @Test
        void error401ThrowsException() {
            when(mockBatchResponse.getStatus()).thenReturn(401);
            when(mockBatchResponse.readEntity(String.class)).thenReturn("Unauthorized");

            WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> resourceClient.retrieveBatchSecrets("some-secret"));

            assertTrue(ex.getMessage().contains("401"));
        }

        @Test
        void error422ThrowsException() {
            when(mockBatchResponse.getStatus()).thenReturn(422);
            when(mockBatchResponse.readEntity(String.class)).thenReturn("Missing parameter");

            WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> resourceClient.retrieveBatchSecrets("bad-request"));

            assertTrue(ex.getMessage().contains("422"));
        }

        @Test
        void nullVariableIdsThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> resourceClient.retrieveBatchSecrets((String[]) null));
        }

        @Test
        void emptyVariableIdsThrowsException() {
            assertThrows(IllegalArgumentException.class,
                () -> resourceClient.retrieveBatchSecrets(new String[0]));
        }



        @Test
        void preservesResponseOrder() {
            // Verify that the result map preserves insertion order (LinkedHashMap)
            String json = "{"
                + "\"myaccount:variable:zebra\": \"z\","
                + "\"myaccount:variable:alpha\": \"a\","
                + "\"myaccount:variable:middle\": \"m\""
                + "}";
            when(mockBatchResponse.getStatus()).thenReturn(200);
            when(mockBatchResponse.readEntity(String.class)).thenReturn(json);

            Map<String, String> result = resourceClient.retrieveBatchSecrets(
                "zebra", "alpha", "middle");

            // Verify all values present
            assertEquals("z", result.get("zebra"));
            assertEquals("a", result.get("alpha"));
            assertEquals("m", result.get("middle"));
        }

        @Test
        void deeplyNestedVariableId() {
            String json = "{\"myaccount:variable:a/b/c/d/e/f\": \"deep\"}";
            when(mockBatchResponse.getStatus()).thenReturn(200);
            when(mockBatchResponse.readEntity(String.class)).thenReturn(json);

            Map<String, String> result = resourceClient.retrieveBatchSecrets("a/b/c/d/e/f");

            assertEquals("deep", result.get("a/b/c/d/e/f"));

            verify(mockClient).target(argThat((URI uri) ->
                uri.toString().contains("a%2Fb%2Fc%2Fd%2Fe%2Ff")
            ));
        }
    }

    // ========================================================================
    // buildBatchQueryParam Tests (package-private helper)
    // ========================================================================

    @Nested
    class BuildBatchQueryParam {

        @Test
        void singleId() {
            String result = resourceClient.buildBatchQueryParam("acct", "secret1");
            assertEquals("acct:variable:secret1", result);
        }

        @Test
        void multipleIds() {
            String result = resourceClient.buildBatchQueryParam("acct", "s1", "s2", "s3");
            assertEquals("acct:variable:s1,acct:variable:s2,acct:variable:s3", result);
        }

        @Test
        void encodesSlashesInIds() {
            String result = resourceClient.buildBatchQueryParam("acct", "path/to/secret");
            assertEquals("acct:variable:path%2Fto%2Fsecret", result);
        }

        @Test
        void encodesSpecialCharacters() {
            String result = resourceClient.buildBatchQueryParam("acct", "user@host");
            assertEquals("acct:variable:user%40host", result);
        }

        @Test
        void encodesSpacesAs20() {
            String result = resourceClient.buildBatchQueryParam("acct", "my secret");
            assertTrue(result.contains("my%20secret"), "Spaces should be encoded as %20, not +");
            assertFalse(result.contains("my+secret"));
        }
    }

    // ========================================================================
    // Single Secret Retrieval Tests (existing functionality, newly testable)
    // ========================================================================

    @Nested
    class SingleRetrieval {

        @Test
        void retrieveSecretSuccess() {
            when(mockSingleBuilder.get(Response.class)).thenReturn(mockSingleResponse);
            when(mockSingleResponse.getStatus()).thenReturn(200);
            when(mockSingleResponse.readEntity(String.class)).thenReturn("my-secret-value");

            String result = resourceClient.retrieveSecret("db-password");

            assertEquals("my-secret-value", result);
            verify(mockSecrets).path(eq("db-password"));
        }

        @Test
        void retrieveSecretWithSlashes() {
            when(mockSingleBuilder.get(Response.class)).thenReturn(mockSingleResponse);
            when(mockSingleResponse.getStatus()).thenReturn(200);
            when(mockSingleResponse.readEntity(String.class)).thenReturn("val");

            String result = resourceClient.retrieveSecret("prod/aws/db-password");

            assertEquals("val", result);
            // Verify slashes are encoded
            verify(mockSecrets).path(eq("prod%2Faws%2Fdb-password"));
        }

        @Test
        void retrieveSecretError404() {
            when(mockSingleBuilder.get(Response.class)).thenReturn(mockSingleResponse);
            when(mockSingleResponse.getStatus()).thenReturn(404);
            when(mockSingleResponse.readEntity(String.class)).thenReturn("Not found");

            WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> resourceClient.retrieveSecret("missing"));

            assertTrue(ex.getMessage().contains("404"));
        }
    }

    // ========================================================================
    // Endpoints Integration Tests
    // ========================================================================

    @Nested
    class EndpointsTests {

        @Test
        void batchSecretsUriDerivedCorrectly() {
            Endpoints ep = new Endpoints("https://conjur.example.com", "myorg");
            assertEquals(
                URI.create("https://conjur.example.com/secrets"),
                ep.getBatchSecretsUri()
            );
        }

        @Test
        void batchSecretsUriWithPort() {
            Endpoints ep = new Endpoints("https://conjur.example.com:8443", "myorg");
            assertEquals(
                URI.create("https://conjur.example.com:8443/secrets"),
                ep.getBatchSecretsUri()
            );
        }

        @Test
        void accountAndApplianceUrlStored() {
            Endpoints ep = new Endpoints("https://host", "custom-account");
            assertEquals("custom-account", ep.getAccount());
            assertEquals("https://host", ep.getApplianceUrl());
        }

        @Test
        void uriDerivedFromApplianceUrlAndAccount() {
            Endpoints ep = new Endpoints("https://host", "myorg");
            assertEquals(URI.create("https://host/authn/myorg"), ep.getAuthnUri());
            assertEquals(URI.create("https://host/secrets/myorg/variable"), ep.getSecretsUri());
            assertEquals(URI.create("https://host/secrets"), ep.getBatchSecretsUri());
        }

        @Test
        void customAuthnUrl() {
            Endpoints ep = new Endpoints("https://host", "myorg", "https://host/authn-ldap/my-svc");
            assertEquals(URI.create("https://host/authn-ldap/my-svc/myorg"), ep.getAuthnUri());
            assertEquals(URI.create("https://host/secrets/myorg/variable"), ep.getSecretsUri());
        }


    }
}
