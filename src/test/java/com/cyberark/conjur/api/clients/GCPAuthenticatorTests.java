package com.cyberark.conjur.api.clients;

import com.cyberark.conjur.api.Token;
import jakarta.ws.rs.client.Client;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class GCPAuthenticatorTests {

    @BeforeAll
    public static void logSuiteStart() {
        System.out.println("### START GCPAuthenticatorTests ###");
    }

    private static class TestableGCPAuthenticator extends GCPAuthenticator {
        private final String metadataJwt;
        private String jwtSentToConjur;
        private int metadataCalls;

        TestableGCPAuthenticator(String initialJwt, String metadataJwt) {
            super(
                    mock(Client.class),
                    mock(Client.class),
                    "https://conjur.example/api",
                    "test-account",
                    "host/data/test/gcp-apps/test-app",
                    "authn-jwt/gcp",
                    initialJwt,
                    "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity"
            );
            this.metadataJwt = metadataJwt;
        }

        @Override
        protected String fetchGcpJwt() {
            metadataCalls++;
            return metadataJwt;
        }

        @Override
        protected Token exchangeJwtForConjurToken(String gcpJwt) {
            jwtSentToConjur = gcpJwt;
            return Token.fromJson("{\"payload\":\"e30=\",\"protected\":\"e30=\",\"signature\":\"sig\"}");
        }
    }

    @Test
    public void refreshJwt_usesExplicitTokenWithoutMetadataCall() {
        TestableGCPAuthenticator authenticator = new TestableGCPAuthenticator("explicit-token", "metadata-token");
        Token token = authenticator.authenticate();
        assertNotNull(token);
        assertEquals("explicit-token", authenticator.jwtSentToConjur);
        assertEquals(0, authenticator.metadataCalls);
    }

    @Test
    public void refreshJwt_fetchesMetadataTokenWhenJwtIsMissing() {
        TestableGCPAuthenticator authenticator = new TestableGCPAuthenticator(null, "metadata-token");
        Token token = authenticator.authenticate();
        assertNotNull(token);
        assertEquals("metadata-token", authenticator.jwtSentToConjur);
        assertEquals(1, authenticator.metadataCalls);
    }

    @Test
    public void buildAudience_removesHostPrefix() {
        GCPAuthenticator authenticator = new GCPAuthenticator(
                mock(Client.class),
                mock(Client.class),
                "https://conjur.example/api",
                "test-account",
                "host/data/test/gcp-apps/test-app",
                "authn-jwt/gcp",
                null,
                GCPAuthenticator.DEFAULT_GCP_IDENTITY_URL
        );

        assertEquals("conjur/test-account/host/data/test/gcp-apps/test-app", authenticator.buildAudience());
    }
}