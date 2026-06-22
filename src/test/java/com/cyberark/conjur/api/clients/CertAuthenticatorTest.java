package com.cyberark.conjur.api.clients;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cyberark.conjur.api.Token;

/**
 * Unit tests for {@link CertAuthenticator}.
 *
 * <p>The JAX-RS client stack is fully mocked so no Conjur server or real TLS handshake
 * is needed. The {@link CertAuthenticator} constructor that accepts an inline PEM string
 * is exercised via a test subclass that overrides {@code buildMtlsClient} — but since that
 * method is private we instead wire a mock {@link Client} using the
 * package-private constructor path provided by reflection.</p>
 *
 * <p>The constructor-level PEM parsing (which actually loads the KeyStore) is tested
 * separately in {@link CertAuthenticatorPemParsingTest}.</p>
 */
public class CertAuthenticatorTest {

    private static final URI TEST_AUTHENTICATE_URI =
            URI.create("https://conjur.example.com/authn-cert/acme-vm/myorg/host%2Fvm-01/authenticate");
    private static final URI TEST_SPIFFE_URI =
            URI.create("https://conjur.example.com/authn-cert/acme-vm/myorg/authenticate");

    // Token JSON that matches the structure Token.fromJson() expects
    // (simplified — real tokens are base64-encoded JWTs, but fromJson wraps the raw string)
    private static final String MOCK_TOKEN_JSON =
            "{\"protected\":\"eyJhbGciOiJSUzI1NiJ9\"," +
            "\"payload\":\"eyJzdWIiOiJ0ZXN0IiwiaWF0IjoiMTcwMDAwMDAwMCJ9\"," +
            "\"signature\":\"sig\"}";

    private Client mockClient;
    private WebTarget mockTarget;
    private Invocation.Builder mockBuilder;
    private Response mockResponse;

    @BeforeEach
    void setUpMocks() {
        mockClient   = mock(Client.class);
        mockTarget   = mock(WebTarget.class);
        mockBuilder  = mock(Invocation.Builder.class);
        mockResponse = mock(Response.class);

        when(mockClient.target(any(URI.class))).thenReturn(mockTarget);
        when(mockTarget.request(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.post(any(Entity.class), eq(Response.class))).thenReturn(mockResponse);
    }

    // -------------------------------------------------------------------------
    // Helper: builds a CertAuthenticator that uses a pre-wired mock HTTP client
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link CertAuthenticator} backed by the supplied mock {@link Client},
     * bypassing the actual mTLS / KeyStore initialisation.
     */
    private CertAuthenticator authenticatorWithMockClient(URI uri) {
        return new CertAuthenticator(uri, mockClient);
    }

    // -------------------------------------------------------------------------
    // authenticate()
    // -------------------------------------------------------------------------

    @Nested
    class Authenticate {

        @Test
        void returnsTokenOnSuccess() {
            when(mockResponse.getStatus()).thenReturn(200);
            when(mockResponse.readEntity(String.class)).thenReturn(MOCK_TOKEN_JSON);

            CertAuthenticator authenticator = authenticatorWithMockClient(TEST_AUTHENTICATE_URI);
            Token token = authenticator.authenticate();

            assertNotNull(token);
            verify(mockClient).target(TEST_AUTHENTICATE_URI);
            verify(mockTarget).request("application/json");
            verify(mockBuilder).post(any(Entity.class), eq(Response.class));
        }

        @Test
        void postIsIssuedToCorrectUri() {
            when(mockResponse.getStatus()).thenReturn(200);
            when(mockResponse.readEntity(String.class)).thenReturn(MOCK_TOKEN_JSON);

            CertAuthenticator authenticator = authenticatorWithMockClient(TEST_AUTHENTICATE_URI);
            authenticator.authenticate();

            verify(mockClient).target(TEST_AUTHENTICATE_URI);
        }

        @Test
        void spiffeModeUsesUriWithoutHostId() {
            when(mockResponse.getStatus()).thenReturn(200);
            when(mockResponse.readEntity(String.class)).thenReturn(MOCK_TOKEN_JSON);

            CertAuthenticator authenticator = authenticatorWithMockClient(TEST_SPIFFE_URI);
            authenticator.authenticate();

            verify(mockClient).target(TEST_SPIFFE_URI);
        }

        @Test
        void throws401WhenUnauthorised() {
            when(mockResponse.getStatus()).thenReturn(401);
            when(mockResponse.readEntity(String.class)).thenReturn("Unauthorized");

            CertAuthenticator authenticator = authenticatorWithMockClient(TEST_AUTHENTICATE_URI);
            WebApplicationException ex = assertThrows(WebApplicationException.class,
                    authenticator::authenticate);

            assertEquals(401, ex.getResponse().getStatus());
        }

        @Test
        void throws404WhenNotFound() {
            when(mockResponse.getStatus()).thenReturn(404);
            when(mockResponse.readEntity(String.class)).thenReturn("Not Found");

            CertAuthenticator authenticator = authenticatorWithMockClient(TEST_AUTHENTICATE_URI);
            WebApplicationException ex = assertThrows(WebApplicationException.class,
                    authenticator::authenticate);

            assertEquals(404, ex.getResponse().getStatus());
        }
    }

    // -------------------------------------------------------------------------
    // authenticate(boolean)
    // -------------------------------------------------------------------------

    @Nested
    class AuthenticateWithCachingFlag {

        @Test
        void trueCallsDelegatesToAuthenticate() {
            when(mockResponse.getStatus()).thenReturn(200);
            when(mockResponse.readEntity(String.class)).thenReturn(MOCK_TOKEN_JSON);

            CertAuthenticator authenticator = authenticatorWithMockClient(TEST_AUTHENTICATE_URI);
            Token token = authenticator.authenticate(true);

            assertNotNull(token);
        }

        @Test
        void falseCallsDelegatesToAuthenticate() {
            when(mockResponse.getStatus()).thenReturn(200);
            when(mockResponse.readEntity(String.class)).thenReturn(MOCK_TOKEN_JSON);

            CertAuthenticator authenticator = authenticatorWithMockClient(TEST_AUTHENTICATE_URI);
            Token token = authenticator.authenticate(false);

            assertNotNull(token);
        }
    }
}

