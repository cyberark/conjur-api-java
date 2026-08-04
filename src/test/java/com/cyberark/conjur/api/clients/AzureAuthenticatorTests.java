package com.cyberark.conjur.api.clients;

import com.cyberark.conjur.api.Token;
import jakarta.ws.rs.client.Client;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class AzureAuthenticatorTests {

    @BeforeAll
    public static void logSuiteStart() {
        System.out.println("### START AzureAuthenticatorTests ###");
    }

    private static class TestableAzureAuthenticator extends AzureAuthenticator {
        private final String azureJwt;
        private String jwtSentToConjur;

        TestableAzureAuthenticator(String azureJwt, String clientId) {
            super(
                    mock(Client.class),
                    mock(Client.class),
                    "https://conjur.example/api",
                    "test-account",
                    "host/data/test/azure-apps/test-app",
                    "authn-azure/test",
                    "https://management.azure.com/",
                    clientId,
                    "http://169.254.169.254",
                    "2018-02-01"
            );
            this.azureJwt = azureJwt;
        }

        @Override
        protected String fetchAzureJwt() {
            return azureJwt;
        }

        @Override
        protected Token exchangeJwtForConjurToken(String azureJwt) {
            jwtSentToConjur = azureJwt;
            return Token.fromJson("{\"payload\":\"e30=\",\"protected\":\"e30=\",\"signature\":\"sig\"}");
        }
    }

    @Test
    public void authenticate_passesAzureJwtToConjur() {
        TestableAzureAuthenticator authenticator = new TestableAzureAuthenticator("explicit-azure-jwt", null);
        Token token = authenticator.authenticate();
        assertNotNull(token);
        assertEquals("explicit-azure-jwt", authenticator.jwtSentToConjur);
    }

    @Test
    public void buildImdsTokenUrl_withoutClientId_omitsClientIdParam() {
        AzureAuthenticator authenticator = new AzureAuthenticator(
                mock(Client.class),
                mock(Client.class),
                "https://conjur.example/api",
                "test-account",
                "host/data/test/azure-apps/test-app",
                "authn-azure/test",
                "https://management.azure.com/",
                null,
                "http://169.254.169.254",
                "2018-02-01"
        );

        String url = authenticator.buildImdsTokenUrl();

        assertTrue(url.contains("api-version=2018-02-01"));
        assertTrue(url.contains("resource=https%3A%2F%2Fmanagement.azure.com%2F"));
        assertFalse(url.contains("client_id="));
    }

    @Test
    public void buildImdsTokenUrl_withClientId_includesClientIdParam() {
        AzureAuthenticator authenticator = new AzureAuthenticator(
                mock(Client.class),
                mock(Client.class),
                "https://conjur.example/api",
                "test-account",
                "host/data/test/azure-apps/test-app",
                "authn-azure/test",
                "https://management.azure.com/",
                "test-client-id",
                "http://169.254.169.254",
                "2018-02-01"
        );

        String url = authenticator.buildImdsTokenUrl();
        assertTrue(url.contains("client_id=test-client-id"));
    }
}