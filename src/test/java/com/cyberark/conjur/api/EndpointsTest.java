package com.cyberark.conjur.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EndpointsTest {

    private static final String ACCOUNT_PROPERTY = Constants.CONJUR_ACCOUNT_PROPERTY;
    private static final String APPLIANCE_URL_PROPERTY = Constants.CONJUR_APPLIANCE_URL_PROPERTY;
    private static final String AUTHN_URL_PROPERTY = Constants.CONJUR_AUTHN_URL_PROPERTY;

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty(ACCOUNT_PROPERTY);
        System.clearProperty(APPLIANCE_URL_PROPERTY);
        System.clearProperty(AUTHN_URL_PROPERTY);
    }

    @Nested
    class Constructors {

        @Test
        void uriConstructorPreservesUrisOnly() {
            Endpoints endpoints = new Endpoints(
                    URI.create("https://conjur.example.com/authn/myorg"),
                    URI.create("https://conjur.example.com/secrets/myorg/variable")
            );

            assertEquals(URI.create("https://conjur.example.com/authn/myorg"), endpoints.getAuthnUri());
            assertEquals(URI.create("https://conjur.example.com/secrets/myorg/variable"), endpoints.getSecretsUri());
            assertThrows(IllegalStateException.class, endpoints::getAccount);
            assertThrows(IllegalStateException.class, endpoints::getBatchSecretsUri);
        }

        @Test
        void uriConstructorDoesNotDeriveBatchUriEvenWithAppliancePathPrefix() {
            Endpoints endpoints = new Endpoints(
                    URI.create("http://localhost:3000/api/authn/myorg"),
                    URI.create("http://localhost:3000/api/secrets/myorg/variable")
            );

            assertThrows(IllegalStateException.class, endpoints::getAccount);
            assertThrows(IllegalStateException.class, endpoints::getBatchSecretsUri);
        }

        @Test
        void stringConstructorPreservesUrisOnly() {
            Endpoints endpoints = new Endpoints(
                    "https://host/authn/demo",
                    "https://host/secrets/demo/variable"
            );

            assertEquals(URI.create("https://host/authn/demo"), endpoints.getAuthnUri());
            assertEquals(URI.create("https://host/secrets/demo/variable"), endpoints.getSecretsUri());
            assertThrows(IllegalStateException.class, endpoints::getAccount);
            assertThrows(IllegalStateException.class, endpoints::getBatchSecretsUri);
        }

        @Test
        void uriConstructorPreservesCustomAuthenticatorUri() {
            Endpoints endpoints = new Endpoints(
                    URI.create("https://gateway.example.com/conjur/authn-ldap/service-1/myorg"),
                    URI.create("https://gateway.example.com/conjur/secrets/myorg/variable")
            );

            assertEquals(
                    URI.create("https://gateway.example.com/conjur/authn-ldap/service-1/myorg"),
                    endpoints.getAuthnUri()
            );
            assertEquals(URI.create("https://gateway.example.com/conjur/secrets/myorg/variable"), endpoints.getSecretsUri());

            assertThrows(IllegalStateException.class, endpoints::getAccount);
            assertThrows(IllegalStateException.class, endpoints::getBatchSecretsUri);
        }
    }

    @Nested
    class PropertyFactories {

        @Test
        void fromSystemPropertiesUsesDefaultAuthnUrlWhenUnset() {
            System.setProperty(ACCOUNT_PROPERTY, "dev");
            System.setProperty(APPLIANCE_URL_PROPERTY, "https://host/api");

            Endpoints endpoints = Endpoints.fromSystemProperties();

            assertEquals("dev", endpoints.getAccount());
            assertEquals(URI.create("https://host/api/authn/dev"), endpoints.getAuthnUri());
            assertEquals(URI.create("https://host/api/secrets/dev/variable"), endpoints.getSecretsUri());
            assertEquals(URI.create("https://host/api/secrets"), endpoints.getBatchSecretsUri());
        }

        @Test
        void fromSystemPropertiesUsesExplicitAuthnUrlWhenProvided() {
            System.setProperty(ACCOUNT_PROPERTY, "dev");
            System.setProperty(APPLIANCE_URL_PROPERTY, "https://host/api");
            System.setProperty(AUTHN_URL_PROPERTY, "https://host/api/authn-jwt/prod");

            Endpoints endpoints = Endpoints.fromSystemProperties();

            assertEquals("dev", endpoints.getAccount());
            assertEquals(URI.create("https://host/api/authn-jwt/prod/dev"), endpoints.getAuthnUri());
            assertEquals(URI.create("https://host/api/secrets/dev/variable"), endpoints.getSecretsUri());
            assertEquals(URI.create("https://host/api/secrets"), endpoints.getBatchSecretsUri());
        }

        @Test
        void fromCredentialsUsesCredentialsAuthnUrl() {
            System.setProperty(ACCOUNT_PROPERTY, "dev");
            System.setProperty(APPLIANCE_URL_PROPERTY, "https://host/api");

            Credentials credentials = new Credentials(
                    "alice",
                    "secret",
                    "https://host/api/authn-ldap/service-1"
            );

            Endpoints endpoints = Endpoints.fromCredentials(credentials);

            assertEquals("dev", endpoints.getAccount());
            assertEquals(URI.create("https://host/api/authn-ldap/service-1/dev"), endpoints.getAuthnUri());
            assertEquals(URI.create("https://host/api/secrets/dev/variable"), endpoints.getSecretsUri());
            assertEquals(URI.create("https://host/api/secrets"), endpoints.getBatchSecretsUri());
        }
    }
}

