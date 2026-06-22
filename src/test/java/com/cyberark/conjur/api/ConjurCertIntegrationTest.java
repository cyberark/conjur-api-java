package com.cyberark.conjur.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * End-to-end integration tests for the authn-cert (mTLS) authenticator.
 *
 * <p>These tests are skipped unless the environment variable {@code TEST_CERT=true} is set.
 * They require a running Conjur server with authn-cert configured and the following
 * environment variables (in addition to the standard {@code CONJUR_ACCOUNT},
 * {@code CONJUR_APPLIANCE_URL}):</p>
 *
 * <ul>
 *   <li>{@code CONJUR_AUTHN_CERT_FILE} – path to the PEM-encoded client certificate</li>
 *   <li>{@code CONJUR_AUTHN_CERT_KEY_FILE} – path to the PEM-encoded private key (PKCS#8)</li>
 *   <li>{@code CONJUR_AUTHN_CERT_SERVICE_ID} – authn-cert service ID (e.g. {@code "acme-vm"})</li>
 *   <li>{@code CONJUR_AUTHN_CERT_HOST_ID} – Conjur host ID for request mode
 *       (e.g. {@code "host/vm-workloads/vm-01"}); omit or leave blank for SPIFFE mode</li>
 * </ul>
 *
 * <p>To use a custom CA for the Conjur server certificate, load it into the JVM cacerts
 * store before running these tests.</p>
 *
 * <h3>Running the tests</h3>
 * <pre>{@code
 * export TEST_CERT=true
 * export CONJUR_ACCOUNT=myorg
 * export CONJUR_APPLIANCE_URL=https://conjur.example.com
 * export CONJUR_AUTHN_CERT_FILE=/path/to/client.pem
 * export CONJUR_AUTHN_CERT_KEY_FILE=/path/to/client-key.pem
 * export CONJUR_AUTHN_CERT_SERVICE_ID=acme-vm
 * export CONJUR_AUTHN_CERT_HOST_ID=host/vm-workloads/vm-01
 * mvn test -Dtest=ConjurCertIntegrationTest
 * }</pre>
 */
public class ConjurCertIntegrationTest {

    @BeforeAll
    static void checkEnabled() {
        assumeTrue(
                "true".equalsIgnoreCase(System.getenv("TEST_CERT")),
                "Skipping certificate authn integration test. Set TEST_CERT=true to enable."
        );
    }

    /**
     * Happy path: authenticate via authn-cert and retrieve a secret.
     *
     * <p>Requires the secret at the path in {@code TEST_CERT_VARIABLE_ID} to be set in Conjur.</p>
     */
    @Test
    void certAuthRequestModeCanRetrieveSecret() throws Exception {
        String variableId = System.getenv("TEST_CERT_VARIABLE_ID");
        assumeTrue(variableId != null && !variableId.isEmpty(),
                "Set TEST_CERT_VARIABLE_ID to a variable the cert host can read");

        Conjur conjur = Conjur.newFromCertificate();
        assertNotNull(conjur);

        String secret = conjur.variables().retrieveSecret(variableId);
        assertNotNull(secret);
        assertFalse(secret.isEmpty(), "Retrieved secret should not be empty");
    }

    /**
     * SPIFFE mode: authenticate with an empty host ID (host derived from cert's SPIFFE SAN URI).
     *
     * <p>Requires {@code CONJUR_AUTHN_CERT_HOST_ID} to be unset or empty and the
     * authn-cert service to be configured for SPIFFE mode.</p>
     */
    @Test
    void certAuthSpiffeModeCanAuthenticate() throws Exception {
        assumeTrue(
                System.getenv("TEST_CERT_SPIFFE") != null &&
                "true".equalsIgnoreCase(System.getenv("TEST_CERT_SPIFFE")),
                "Skipping SPIFFE mode test. Set TEST_CERT_SPIFFE=true to enable."
        );

        // Temporarily override host-id to empty for SPIFFE mode
        // The standard env-var path picks up CONJUR_AUTHN_CERT_HOST_ID="" if set.
        Conjur conjur = Conjur.newFromCertificate();
        assertNotNull(conjur);
    }
}

