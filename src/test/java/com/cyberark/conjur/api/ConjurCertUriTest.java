package com.cyberark.conjur.api;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the cert-authenticator URI-building logic in {@link Conjur} and
 * {@link Endpoints#getCertAuthnBaseUri(String)}.
 */
public class ConjurCertUriTest {

    private static final String ACCOUNT_PROPERTY      = Constants.CONJUR_ACCOUNT_PROPERTY;
    private static final String APPLIANCE_URL_PROPERTY = Constants.CONJUR_APPLIANCE_URL_PROPERTY;
    private static final String AUTHN_URL_PROPERTY     = Constants.CONJUR_AUTHN_URL_PROPERTY;

    @BeforeEach
    void setUp() {
        System.setProperty(ACCOUNT_PROPERTY, "myorg");
        System.setProperty(APPLIANCE_URL_PROPERTY, "https://conjur.example.com");
        System.setProperty(AUTHN_URL_PROPERTY, "https://conjur.example.com/authn");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(ACCOUNT_PROPERTY);
        System.clearProperty(APPLIANCE_URL_PROPERTY);
        System.clearProperty(AUTHN_URL_PROPERTY);
    }

    // -------------------------------------------------------------------------
    // Endpoints.getCertAuthnBaseUri
    // -------------------------------------------------------------------------

    @Nested
    class CertAuthnBaseUri {

        @Test
        void basicServiceId() {
            Endpoints endpoints = Endpoints.fromSystemProperties();
            URI uri = endpoints.getCertAuthnBaseUri("acme-vm");
            assertEquals(URI.create("https://conjur.example.com/authn-cert/acme-vm/myorg"), uri);
        }

        @Test
        void serviceIdWithSpecialCharactersIsEncoded() {
            Endpoints endpoints = Endpoints.fromSystemProperties();
            URI uri = endpoints.getCertAuthnBaseUri("my service");
            assertEquals(URI.create("https://conjur.example.com/authn-cert/my%20service/myorg"), uri);
        }

        @Test
        void throwsWhenEndpointsCreatedWithDeprecatedConstructor() {
            @SuppressWarnings("deprecation")
            Endpoints endpoints = new Endpoints(
                    URI.create("https://conjur.example.com/authn/myorg"),
                    URI.create("https://conjur.example.com/secrets/myorg/variable"));
            assertThrows(IllegalStateException.class, () -> endpoints.getCertAuthnBaseUri("svc"));
        }
    }

    // -------------------------------------------------------------------------
    // Conjur.buildCertAuthenticateUri
    // -------------------------------------------------------------------------

    @Nested
    class BuildCertAuthenticateUri {

        @Test
        void requestModeAppendsEncodedHostIdAndAuthenticate() {
            Endpoints endpoints = Endpoints.fromSystemProperties();
            URI uri = Conjur.buildCertAuthenticateUri(endpoints, "acme-vm", "host/vm-workloads/vm-01");
            assertEquals(
                    URI.create("https://conjur.example.com/authn-cert/acme-vm/myorg/host%2Fvm-workloads%2Fvm-01/authenticate"),
                    uri);
        }

        @Test
        void spiffeModeWithNullHostIdOmitsHostSegment() {
            Endpoints endpoints = Endpoints.fromSystemProperties();
            URI uri = Conjur.buildCertAuthenticateUri(endpoints, "acme-vm", null);
            assertEquals(
                    URI.create("https://conjur.example.com/authn-cert/acme-vm/myorg/authenticate"),
                    uri);
        }

        @Test
        void spiffeModeWithEmptyHostIdOmitsHostSegment() {
            Endpoints endpoints = Endpoints.fromSystemProperties();
            URI uri = Conjur.buildCertAuthenticateUri(endpoints, "acme-vm", "");
            assertEquals(
                    URI.create("https://conjur.example.com/authn-cert/acme-vm/myorg/authenticate"),
                    uri);
        }

        @Test
        void hostIdWithSpacesIsEncoded() {
            Endpoints endpoints = Endpoints.fromSystemProperties();
            URI uri = Conjur.buildCertAuthenticateUri(endpoints, "acme-vm", "host/my host");
            assertTrue(uri.toString().contains("my%20host"));
        }

        @Test
        void slashesInHostIdAreEncoded() {
            Endpoints endpoints = Endpoints.fromSystemProperties();
            URI uri = Conjur.buildCertAuthenticateUri(endpoints, "acme-vm", "host/vm-01");
            // Forward slashes in the host ID must be percent-encoded
            assertTrue(uri.toString().contains("host%2Fvm-01"));
        }
    }
}

