package com.cyberark.conjur.api.clients;

import com.cyberark.conjur.api.Conjur;
import com.cyberark.conjur.api.Credentials;
import com.cyberark.conjur.api.Endpoints;
import com.cyberark.conjur.api.Token;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GCPAuthenticatorIntegrationTests {

    private static final String CONJUR_IDENTITY = "host/data/test/gcp-apps/test-app";
    private static final String CONJUR_AUTHENTICATOR = "authn-jwt/gcp";

    private static final String AUTHN_JWT_GCP_POLICY =
            "- !policy\n" +
            "  id: gcp\n" +
            "  body:\n" +
            "  - !webservice\n" +
            "\n" +
            "  - !variable\n" +
            "    id: provider-uri\n" +
            "\n" +
            "  - !group apps\n" +
            "\n" +
            "  - !permit\n" +
            "    role: !group apps\n" +
            "    privilege: [ read, authenticate ]\n" +
            "    resource: !webservice\n" +
            "\n" +
            "  - !grant\n" +
            "    role: !group apps\n" +
            "    member: !host /data/test/gcp-apps/test-app\n";

    private static final String GCP_ROLES_POLICY_TEMPLATE =
            "- !policy\n" +
            "  id: gcp-apps\n" +
            "  body:\n" +
            "  - &variables\n" +
            "    - !variable database/username\n" +
            "    - !variable database/password\n" +
            "  - !group secrets-users\n" +
            "  - !permit\n" +
            "    role: !group secrets-users\n" +
            "    privilege: [ read, execute ]\n" +
            "    resource: *variables\n" +
            "\n" +
            "  - !group\n" +
            "  - !host\n" +
            "    id: test-app\n" +
            "    annotations:\n" +
            "      authn-jwt/gcp/google/compute_engine/project_id: \"%s\"\n" +
            "  - !grant\n" +
            "    role: !group\n" +
            "    member: !host test-app\n" +
            "  - !grant\n" +
            "    member: !group\n" +
            "    role: !group secrets-users\n";

    @Test
    public void authnGcpWithExplicitToken() {
        assumeGcpEnabled();

        String gcpIdToken = requireEnv("GCP_ID_TOKEN");
        String gcpProjectId = requireEnv("GCP_PROJECT_ID");

        Credentials adminCredentials = Credentials.fromSystemProperties();
        Conjur adminConjur = new Conjur(adminCredentials);
        Token adminToken = new AuthnClient(adminCredentials, Endpoints.fromCredentials(adminCredentials)).authenticate();

        ensureDataTestPolicyExists(adminToken);
        ensureAuthnJwtPolicyExists(adminToken);

        // Load the host first so the grant in the authenticator policy can reference it
        String rolesPolicy = String.format(GCP_ROLES_POLICY_TEMPLATE, gcpProjectId);
        loadPolicy(adminToken, "data/test", rolesPolicy, "POST");

        ensureAuthnJwtGcpPolicyExists(adminToken);

        adminConjur.variables().addSecret("conjur/authn-jwt/gcp/provider-uri",
                "https://accounts.google.com");
        adminConjur.variables().addSecret("data/test/gcp-apps/database/username", "secret");
        adminConjur.variables().addSecret("data/test/gcp-apps/database/password", "P@ssw0rd!");

        GCPAuthenticator authenticator = new GCPAuthenticator(
                ClientBuilder.newClient(),
                ClientBuilder.newClient(),
                requireEnv("CONJUR_APPLIANCE_URL"),
                requireEnv("CONJUR_ACCOUNT"),
                CONJUR_IDENTITY,
                CONJUR_AUTHENTICATOR,
                gcpIdToken,
                GCPAuthenticator.DEFAULT_GCP_IDENTITY_URL
        );

        Token hostToken = authenticator.authenticate();
        assertNotNull(hostToken);

        assertEquals("secret", adminConjur.variables().retrieveSecret("data/test/gcp-apps/database/username"));
        assertEquals("P@ssw0rd!", adminConjur.variables().retrieveSecret("data/test/gcp-apps/database/password"));
    }

    private static void assumeGcpEnabled() {
        Assumptions.assumeTrue(
                "true".equalsIgnoreCase(System.getenv("RUN_GCP_TESTS")),
                "Skipping GCP integration tests. Set RUN_GCP_TESTS=true to run."
        );
    }

    private static void ensureDataTestPolicyExists(Token adminToken) {
        String bootstrapPolicy =
                "- !policy\n" +
                "  id: data\n" +
                "  body:\n" +
                "  - !policy\n" +
                "    id: test\n";
        loadPolicy(adminToken, "root", bootstrapPolicy, "PUT");
    }

    private static void ensureAuthnJwtPolicyExists(Token adminToken) {
        String bootstrapPolicy =
                "- !policy\n" +
                "  id: conjur\n" +
                "  body:\n" +
                "  - !policy\n" +
                "    id: authn-jwt\n";
        loadPolicy(adminToken, "root", bootstrapPolicy, "PATCH");
    }

    private static void ensureAuthnJwtGcpPolicyExists(Token adminToken) {
        loadPolicy(adminToken, "conjur/authn-jwt", AUTHN_JWT_GCP_POLICY, "PATCH");
    }

    private static void loadPolicy(Token adminToken, String policyBranch, String yaml, String method) {
        String applianceUrl = requireEnv("CONJUR_APPLIANCE_URL").replaceAll("/+$", "");
        String account = requireEnv("CONJUR_ACCOUNT");
        String url = applianceUrl + "/policies/" + encodeSegment(account) + "/policy/" + encodePath(policyBranch);

        // HttpURLConnection does not support PATCH; use POST + X-HTTP-Method-Override instead
        String httpMethod = "PATCH".equals(method) ? "POST" : method;
        String methodOverride = "PATCH".equals(method) ? "PATCH" : null;

        Client client = ClientBuilder.newClient();
        try {
            jakarta.ws.rs.client.Invocation.Builder builder = client.target(url)
                    .request()
                    .header("Authorization", adminToken.toHeader());
            if (methodOverride != null) {
                builder = builder.header("X-HTTP-Method-Override", methodOverride);
            }
            try (Response response = builder.method(httpMethod, Entity.text(yaml))) {
                int status = response.getStatus();
                String body = response.readEntity(String.class);
                assertTrue(status >= 200 && status < 300,
                        "Policy load failed: status=" + status + " body=" + body + " url=" + url);
            }
        } finally {
            client.close();
        }
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        assertTrue(value != null && !value.trim().isEmpty(), name + " must be set");
        return value;
    }

    private static String encodePath(String path) {
        String[] segments = path.split("/");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                out.append('/');
            }
            out.append(encodeSegment(segments[i]));
        }
        return out.toString();
    }

    private static String encodeSegment(String segment) {
        try {
            return URLEncoder.encode(segment, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode path segment", e);
        }
    }
}
