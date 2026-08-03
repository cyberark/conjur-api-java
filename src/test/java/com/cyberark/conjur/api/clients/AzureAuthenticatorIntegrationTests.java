package com.cyberark.conjur.api.clients;

import com.cyberark.conjur.api.Conjur;
import com.cyberark.conjur.api.Credentials;
import com.cyberark.conjur.api.Endpoints;
import com.cyberark.conjur.api.Token;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureAuthenticatorIntegrationTests {

    private static final String IMDS_RESOURCE = "https://management.azure.com/";
    private static final String CONJUR_IDENTITY = "host/data/test/azure-apps/azureVM";
    private static final String CONJUR_AUTHENTICATOR = "authn-azure/prod";

    private static final String AUTHN_AZURE_POLICY =
            "- !policy\n" +
            "  id: prod\n" +
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
            "    member: !host /data/test/azure-apps/azureVM\n";

    private static final String AUTH_AZURE_ROLES_POLICY_TEMPLATE =
            "- !policy\n" +
            "  id: azure-apps\n" +
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
            "    id: azureVM\n" +
            "    annotations:\n" +
            "      authn-azure/subscription-id: %s\n" +
            "      authn-azure/resource-group: %s\n" +
            "%s" +
            "  - !grant\n" +
            "    role: !group\n" +
            "    member: !host azureVM\n" +
            "  - !grant\n" +
            "    member: !group\n" +
            "    role: !group secrets-users\n";

    @Test
    public void authnAzureSystemAssignedIdentity() {
        assumeAzureEnabled();

        String subscriptionId = requireEnv("AZURE_SUBSCRIPTION_ID");
        String resourceGroup = requireEnv("AZURE_RESOURCE_GROUP");

        Credentials adminCredentials = Credentials.fromSystemProperties();
        Conjur adminConjur = new Conjur(adminCredentials);
        Token adminToken = new AuthnClient(adminCredentials, Endpoints.fromCredentials(adminCredentials)).authenticate();

        ensureDataTestPolicyExists(adminToken);
        ensureAuthnAzurePolicyExists(adminToken);

        String rolesPolicy = String.format(AUTH_AZURE_ROLES_POLICY_TEMPLATE, subscriptionId, resourceGroup, "");
        loadPolicy(adminToken, "data/test", rolesPolicy, "POST");
        loadPolicy(adminToken, "conjur/authn-azure", AUTHN_AZURE_POLICY, "POST");

        adminConjur.variables().addSecret("conjur/authn-azure/prod/provider-uri",
                "https://sts.windows.net/df242c82-fe4a-47e0-b0f4-e3cb7f8104f1/");
        adminConjur.variables().addSecret("data/test/azure-apps/database/username", "secret");
        adminConjur.variables().addSecret("data/test/azure-apps/database/password", "P@ssw0rd!");

        AzureAuthenticator authenticator = new AzureAuthenticator(
                requireEnv("CONJUR_APPLIANCE_URL"),
                requireEnv("CONJUR_ACCOUNT"),
                CONJUR_IDENTITY,
                CONJUR_AUTHENTICATOR
        );

        Token hostToken = authenticator.authenticate();
        assertNotNull(hostToken);

        assertEquals("secret", adminConjur.variables().retrieveSecret("data/test/azure-apps/database/username"));
        assertEquals("P@ssw0rd!", adminConjur.variables().retrieveSecret("data/test/azure-apps/database/password"));
    }

    @Test
    public void authnAzureUserAssignedIdentity() {
        assumeAzureEnabled();

        String subscriptionId = requireEnv("AZURE_SUBSCRIPTION_ID");
        String resourceGroup = requireEnv("AZURE_RESOURCE_GROUP");
        String userAssignedIdentity = requireEnv("USER_ASSIGNED_IDENTITY");
        String userAssignedIdentityClientId = requireEnv("USER_ASSIGNED_IDENTITY_CLIENT_ID");

        Credentials adminCredentials = Credentials.fromSystemProperties();
        Conjur adminConjur = new Conjur(adminCredentials);
        Token adminToken = new AuthnClient(adminCredentials, Endpoints.fromCredentials(adminCredentials)).authenticate();

        ensureDataTestPolicyExists(adminToken);
        ensureAuthnAzurePolicyExists(adminToken);

        String userIdentityAnnotation =
                "      authn-azure/user-assigned-identity: \"" + userAssignedIdentity + "\"\n";
        String rolesPolicy = String.format(
                AUTH_AZURE_ROLES_POLICY_TEMPLATE,
                subscriptionId,
                resourceGroup,
                userIdentityAnnotation
        );

        loadPolicy(adminToken, "data/test", rolesPolicy, "POST");
        loadPolicy(adminToken, "conjur/authn-azure", AUTHN_AZURE_POLICY, "POST");

        String providerUri = resolveProviderUriFromImds(userAssignedIdentityClientId);
        adminConjur.variables().addSecret("conjur/authn-azure/prod/provider-uri", providerUri);
        adminConjur.variables().addSecret("data/test/azure-apps/database/username", "secret");
        adminConjur.variables().addSecret("data/test/azure-apps/database/password", "P@ssw0rd!");

        AzureAuthenticator authenticator = new AzureAuthenticator(
                requireEnv("CONJUR_APPLIANCE_URL"),
                requireEnv("CONJUR_ACCOUNT"),
                CONJUR_IDENTITY,
                CONJUR_AUTHENTICATOR,
                userAssignedIdentityClientId
        );

        Token hostToken = authenticator.authenticate();
        assertNotNull(hostToken);

        assertEquals("secret", adminConjur.variables().retrieveSecret("data/test/azure-apps/database/username"));
        assertEquals("P@ssw0rd!", adminConjur.variables().retrieveSecret("data/test/azure-apps/database/password"));
    }

    private static void assumeAzureEnabled() {
        Assumptions.assumeTrue(
                "true".equalsIgnoreCase(System.getenv("RUN_AZURE_TESTS")),
                "Skipping Azure integration tests. Set RUN_AZURE_TESTS=true to run."
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

    private static void ensureAuthnAzurePolicyExists(Token adminToken) {
        String bootstrapPolicy =
                "- !policy\n" +
                "  id: conjur\n" +
                "  body:\n" +
                "  - !policy\n" +
                "    id: authn-azure\n";
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

    private static String resolveProviderUriFromImds(String clientId) {
        try {
            String imdsUrl = "http://169.254.169.254/metadata/identity/oauth2/token" +
                    "?api-version=2018-02-01" +
                    "&resource=" + URLEncoder.encode(IMDS_RESOURCE, StandardCharsets.UTF_8.name()).replace("+", "%20");

            if (clientId != null && !clientId.isEmpty()) {
                imdsUrl += "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()).replace("+", "%20");
            }

            HttpURLConnection conn = (HttpURLConnection) new URL(imdsUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Metadata", "true");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);

            int code = conn.getResponseCode();
            assertTrue(code >= 200 && code < 300, "IMDS call failed with status " + code);

            String json;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                json = reader.lines().collect(Collectors.joining("\n"));
            }

            JsonObject obj = new Gson().fromJson(json, JsonObject.class);
            String jwt = obj.get("access_token").getAsString();
            assertNotNull(jwt);

            String issuer = extractIssuer(jwt);
            return issuer.endsWith("/") ? issuer : issuer + "/";
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve provider-uri from Azure IMDS", e);
        }
    }

    private static String extractIssuer(String jwt) {
        String[] parts = jwt.split("\\.");
        assertTrue(parts.length >= 2, "Invalid JWT format from IMDS");

        String payload = parts[1];
        byte[] decoded = Base64.getUrlDecoder().decode(payload);
        JsonObject obj = new Gson().fromJson(new String(decoded, StandardCharsets.UTF_8), JsonObject.class);

        assertTrue(obj.has("iss"), "JWT does not contain 'iss' claim");
        return obj.get("iss").getAsString();
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
