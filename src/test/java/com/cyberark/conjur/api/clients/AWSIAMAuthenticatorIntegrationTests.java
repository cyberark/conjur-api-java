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

import static org.junit.jupiter.api.Assertions.*;

public class AWSIAMAuthenticatorIntegrationTests {

    private static final String CONJUR_AUTHENTICATOR = "authn-iam/prod";

    private static final String AUTHN_IAM_POLICY_TEMPLATE =
            "- !policy\n" +
            "  id: prod\n" +
            "  body:\n" +
            "  - !webservice\n" +
            "\n" +
            "  - !group clients\n" +
            "\n" +
            "  - !permit\n" +
            "    role: !group clients\n" +
            "    privilege: [ read, authenticate ]\n" +
            "    resource: !webservice\n" +
            "\n" +
            "  - !grant\n" +
            "    role: !group clients\n" +
            "    member: !host /data/test/myspace/%s/%s\n";

    private static final String IAM_ROLES_POLICY_TEMPLATE =
            "- !policy\n" +
            "  id: myspace\n" +
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
            "  - !layer\n" +
            "  - !host\n" +
            "    id: \"%s/%s\"\n" +
            "  - !grant\n" +
            "    role: !layer\n" +
            "    member: !host \"%s/%s\"\n" +
            "  - !grant\n" +
            "    member: !layer\n" +
            "    role: !group secrets-users\n";

    @Test
    public void authnIamWithInstanceProfile() {
        assumeAwsEnabled();

        String awsAccountId = requireEnv("AWS_ACCOUNT_ID");
        String awsRoleName = requireEnv("AWS_ROLE_NAME");
        String conjurIdentity = "host/data/test/myspace/" + awsAccountId + "/" + awsRoleName;

        Credentials adminCredentials = Credentials.fromSystemProperties();
        Conjur adminConjur = new Conjur(adminCredentials);
        Token adminToken = new AuthnClient(adminCredentials, Endpoints.fromCredentials(adminCredentials)).authenticate();

        ensureDataTestPolicyExists(adminToken);
        ensureAuthnIamPolicyExists(adminToken);

        // Load host policy first so the grant in authn-iam policy can reference it
        String rolesPolicy = String.format(IAM_ROLES_POLICY_TEMPLATE,
                awsAccountId, awsRoleName,
                awsAccountId, awsRoleName,
                awsAccountId, awsRoleName);
        loadPolicy(adminToken, "data/test", rolesPolicy, "POST");

        String authnIamPolicy = String.format(AUTHN_IAM_POLICY_TEMPLATE, awsAccountId, awsRoleName);
        loadPolicy(adminToken, "conjur/authn-iam", authnIamPolicy, "POST");

        adminConjur.variables().addSecret("data/test/myspace/database/username", "secret");
        adminConjur.variables().addSecret("data/test/myspace/database/password", "P@ssw0rd!");

        AWSIAMAuthenticator authenticator = new AWSIAMAuthenticator(
                requireEnv("CONJUR_APPLIANCE_URL"),
                requireEnv("CONJUR_ACCOUNT"),
                conjurIdentity
        );

        Token hostToken = authenticator.authenticate();
        assertNotNull(hostToken);

        assertEquals("secret", adminConjur.variables().retrieveSecret("data/test/myspace/database/username"));
        assertEquals("P@ssw0rd!", adminConjur.variables().retrieveSecret("data/test/myspace/database/password"));
    }

    private static void assumeAwsEnabled() {
        Assumptions.assumeTrue(
                "true".equalsIgnoreCase(System.getenv("RUN_AWS_TESTS")),
                "Skipping AWS IAM integration tests. Set RUN_AWS_TESTS=true to run."
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

    private static void ensureAuthnIamPolicyExists(Token adminToken) {
        String bootstrapPolicy =
                "- !policy\n" +
                "  id: conjur\n" +
                "  body:\n" +
                "  - !policy\n" +
                "    id: authn-iam\n";
        loadPolicy(adminToken, "root", bootstrapPolicy, "PATCH");
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
