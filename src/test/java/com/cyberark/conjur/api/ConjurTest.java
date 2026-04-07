package com.cyberark.conjur.api;

import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Test for the Conjur class
 *
 * Before running this test, verify that:
 *  - Conjur CE is running, healthy and accessible
 *  - A Policy that provides permission for this application to access a secret is loaded
 *  - This policy has an account and a variable named 'test/testVariable' related to that account
 *  - The following system properties are loaded:
 *      * CONJUR_ACCOUNT=myorg
 *      * CONJUR_AUTHN_LOGIN=host/myhost.example.com
 *      * CONJUR_AUTHN_API_KEY=sb0ncv1yj9c4w2e9pb1a2s
 *      * CONJUR_APPLIANCE_URL=https://conjur.myorg.com/api
 */
public class ConjurTest {

    private static final String VARIABLE_KEY = "test/testVariable";
    private static final String VARIABLE_KEY_WITH_SPACES = "test/var with spaces";
    private static final String HOST_KEY = "test/myapp";
    private static final String VARIABLE_VALUE = "testSecret";
    private static final String NON_EXISTING_VARIABLE_KEY = UUID.randomUUID().toString();
    private static final int NOT_FOUND_STATUS_CODE = 404;
    private static final int UNAUTHORIZED_STATUS_CODE = 401;
    private static final String ALTERNATIVE_USERNAME = "host/73789374/iam-role-name";
    private static final String ALTERNATIVE_API_KEY = "notRealApiKey";
    private static final String ALTERNATIVE_AUTHN_ENDPOINT = "/authn-iam/test";
    private static final String APPLIANCE_URL_PROPERTY = "CONJUR_APPLIANCE_URL";
    private static final String ACCOUNT_PROPERTY = "CONJUR_ACCOUNT";
    private static final String LOGIN_PROPERTY = "CONJUR_AUTHN_LOGIN";


    @Test
    public void testLogin() {
        Conjur conjur = new Conjur();

        // The Conjur object is returned with an Authn client logged in
        Assertions.assertNotNull(conjur);
    }

    @Test
    public void testAddSecretAndRetrieveSecret() {
        Conjur conjur = new Conjur();

        String[] variableIds = {
            VARIABLE_KEY,
            VARIABLE_KEY_WITH_SPACES
        };

        String retrievedSecret;
        for (String variableId : variableIds)
        {
            conjur.variables().addSecret(variableId, VARIABLE_VALUE);

            retrievedSecret = conjur.variables().retrieveSecret(variableId);

            Assertions.assertEquals(VARIABLE_VALUE, retrievedSecret);
        }
    }

    @Test
    public void testSetVariableWithoutVariableInPolicy() {
        WebApplicationException thrown = Assertions.assertThrows(
            WebApplicationException.class,
            () -> {
                Conjur conjur = new Conjur();
                conjur.variables().addSecret(NON_EXISTING_VARIABLE_KEY, VARIABLE_VALUE);
            }
        );
        Assertions.assertEquals(NOT_FOUND_STATUS_CODE, thrown.getResponse().getStatus());
    }

    @Test
    public void testRetrieveBatchSecrets() {
        Conjur conjur = new Conjur();

        conjur.variables().addSecret(VARIABLE_KEY, VARIABLE_VALUE);
        conjur.variables().addSecret(VARIABLE_KEY_WITH_SPACES, VARIABLE_VALUE);

        Map<String, String> secrets = conjur.variables().retrieveBatchSecrets(
            VARIABLE_KEY,
            VARIABLE_KEY_WITH_SPACES
        );

        Assertions.assertFalse(secrets.isEmpty());
        Assertions.assertEquals(2, secrets.size());
        Assertions.assertEquals(VARIABLE_VALUE, secrets.get(VARIABLE_KEY));
        Assertions.assertEquals(VARIABLE_VALUE, secrets.get(VARIABLE_KEY_WITH_SPACES));
    }

    @Test
    public void testListResourcesReturnsKnownVariables() {
        Conjur conjur = new Conjur();

        List<ConjurResource> resources = conjur.resources().listResources(
            ResourceQuery.builder().kind("variable").build()
        );

        Assertions.assertFalse(resources.isEmpty());
        Assertions.assertTrue(resources.stream().anyMatch(r -> VARIABLE_KEY.equals(r.getIdentifier())));
        Assertions.assertTrue(resources.stream().anyMatch(r -> VARIABLE_KEY_WITH_SPACES.equals(r.getIdentifier())));
    }

    @Test
    public void testListResourcesSupportsSearchFilter() {
        Conjur conjur = new Conjur();
        ResourceQuery query = ResourceQuery.builder().kind("variable").search("testVariable").build();

        List<ConjurResource> resources = conjur.resources().listResources(query);

        Assertions.assertFalse(resources.isEmpty());
        Assertions.assertTrue(resources.stream().allMatch(r -> "variable".equals(r.getKind())));
        Assertions.assertTrue(resources.stream().anyMatch(r -> VARIABLE_KEY.equals(r.getIdentifier())));
    }

    @Test
    public void testListResourcesSupportsPagination() {
        Conjur conjur = new Conjur();
        ResourceQuery query = ResourceQuery.builder().kind("variable").limit(1).offset(0).build();

        List<ConjurResource> resources = conjur.resources().listResources(query);

        Assertions.assertFalse(resources.isEmpty());
        Assertions.assertTrue(resources.size() <= 1);
        Assertions.assertTrue(resources.stream().allMatch(r -> "variable".equals(r.getKind())));
    }

    @Test
    public void testCountResourcesReturnsKnownVariables() {
        Conjur conjur = new Conjur();

        int count = conjur.resources().countResources(
            ResourceQuery.builder().kind("variable").build()
        );

        // Root test policy loads two variables: test/testVariable and test/var with spaces.
        Assertions.assertTrue(count >= 2);
    }

    @Test
    public void testCountResourcesSupportsSearchFilter() {
        Conjur conjur = new Conjur();
        ResourceQuery query = ResourceQuery.builder().kind("variable").search("testVariable").build();

        int count = conjur.resources().countResources(query);
        List<ConjurResource> resources = conjur.resources().listResources(query);

        Assertions.assertTrue(count >= 1);
        Assertions.assertEquals(resources.size(), count);
    }

    @Test
    public void testListResourcesSupportsHostKindSearchFilter() {
        Assumptions.assumeTrue(isAdminLogin(), "Skipping host-kind tests for non-admin credential runs");

        Conjur conjur = new Conjur();
        ResourceQuery query = ResourceQuery.builder().kind("host").search("myapp").build();

        List<ConjurResource> resources = conjur.resources().listResources(query);

        Assertions.assertFalse(resources.isEmpty());
        Assertions.assertTrue(resources.stream().allMatch(r -> "host".equals(r.getKind())));
        Assertions.assertTrue(resources.stream().anyMatch(r -> HOST_KEY.equals(r.getIdentifier())));
    }

    @Test
    public void testCountResourcesSupportsHostKindSearchFilter() {
        Assumptions.assumeTrue(isAdminLogin(), "Skipping host-kind tests for non-admin credential runs");

        Conjur conjur = new Conjur();
        ResourceQuery query = ResourceQuery.builder().kind("host").search("myapp").build();

        int count = conjur.resources().countResources(query);
        List<ConjurResource> resources = conjur.resources().listResources(query);

        Assertions.assertTrue(count >= 1);
        Assertions.assertTrue(resources.stream().allMatch(r -> "host".equals(r.getKind())));
        Assertions.assertEquals(resources.size(), count);
        Assertions.assertTrue(resources.stream().anyMatch(r -> HOST_KEY.equals(r.getIdentifier())));
    }

    @Test
    public void testListResourcesSupportsActingAsFilter() {
        Conjur conjur = new Conjur();
        String actingAs = getActingAsForCurrentLogin();

        List<ConjurResource> resources = conjur.resources().listResources(
            ResourceQuery.builder().kind("variable").actingAs(actingAs).build()
        );

        Assertions.assertFalse(resources.isEmpty());
        Assertions.assertTrue(resources.stream().allMatch(r -> "variable".equals(r.getKind())));
        Assertions.assertTrue(resources.stream().anyMatch(r -> VARIABLE_KEY.equals(r.getIdentifier())));
        Assertions.assertTrue(resources.stream().anyMatch(r -> VARIABLE_KEY_WITH_SPACES.equals(r.getIdentifier())));
    }

    @Test
    public void testCountResourcesSupportsActingAsFilter() {
        Conjur conjur = new Conjur();
        String actingAs = getActingAsForCurrentLogin();
        ResourceQuery query = ResourceQuery.builder().kind("variable").actingAs(actingAs).build();

        int count = conjur.resources().countResources(query);
        List<ConjurResource> resources = conjur.resources().listResources(query);

        Assertions.assertTrue(count >= 2);
        Assertions.assertEquals(resources.size(), count);
    }

    @Test
    public void testLogonWithAlterativeAuthenticator() {
        String authnUrl = System.getProperty(APPLIANCE_URL_PROPERTY, System.getenv(APPLIANCE_URL_PROPERTY)) + ALTERNATIVE_AUTHN_ENDPOINT;

        WebApplicationException thrown = Assertions.assertThrows(
            WebApplicationException.class,
            () -> {
                Conjur conjur = new Conjur(ALTERNATIVE_USERNAME, ALTERNATIVE_API_KEY, authnUrl);
                conjur.variables().retrieveSecret(VARIABLE_KEY);
            }
        );
        Assertions.assertEquals(UNAUTHORIZED_STATUS_CODE, thrown.getResponse().getStatus());
    }

    @Test
    public void testListResourcesWithAlternativeAuthenticator() {
        String authnUrl = System.getProperty(APPLIANCE_URL_PROPERTY, System.getenv(APPLIANCE_URL_PROPERTY)) + ALTERNATIVE_AUTHN_ENDPOINT;

        WebApplicationException thrown = Assertions.assertThrows(
            WebApplicationException.class,
            () -> {
                Conjur conjur = new Conjur(ALTERNATIVE_USERNAME, ALTERNATIVE_API_KEY, authnUrl);
                conjur.resources().listResources(ResourceQuery.builder().kind("variable").build());
            }
        );
        Assertions.assertEquals(UNAUTHORIZED_STATUS_CODE, thrown.getResponse().getStatus());
    }

    @Test
    public void testCountResourcesWithAlternativeAuthenticator() {
        String authnUrl = System.getProperty(APPLIANCE_URL_PROPERTY, System.getenv(APPLIANCE_URL_PROPERTY)) + ALTERNATIVE_AUTHN_ENDPOINT;

        WebApplicationException thrown = Assertions.assertThrows(
            WebApplicationException.class,
            () -> {
                Conjur conjur = new Conjur(ALTERNATIVE_USERNAME, ALTERNATIVE_API_KEY, authnUrl);
                conjur.resources().countResources(ResourceQuery.builder().kind("variable").build());
            }
        );
        Assertions.assertEquals(UNAUTHORIZED_STATUS_CODE, thrown.getResponse().getStatus());
    }

    private String getAccount() {
        return System.getProperty(ACCOUNT_PROPERTY, System.getenv().getOrDefault(ACCOUNT_PROPERTY, "cucumber"));
    }

    private String getActingAsForCurrentLogin() {
        String login = System.getProperty(LOGIN_PROPERTY, System.getenv().getOrDefault(LOGIN_PROPERTY, "admin"));

        if (login.startsWith("host/")) {
            return getAccount() + ":host:" + login.substring("host/".length());
        }

        return getAccount() + ":user:" + login;
    }

    private boolean isAdminLogin() {
        String login = System.getProperty(LOGIN_PROPERTY, System.getenv().getOrDefault(LOGIN_PROPERTY, "admin"));
        return "admin".equals(login);
    }
}
