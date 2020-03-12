package net.conjur.api;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
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
    private static final String VARIABLE_VALUE = "testSecret";
    private static final String NON_EXISTING_VARIABLE_KEY = UUID.randomUUID().toString();
    private static final String NOT_FOUND_STATUS_CODE = "404";
    private static final String UNAUTHORIZED_STATUS_CODE = "401";
    private static final String ALTERNATIVE_USERNAME = "host/73789374/iam-role-name";
    private static final String ALTERNATIVE_API_KEY = "notRealApiKey";
    private static final String ALTERNATIVE_AUTHN_ENDPOINT = "/authn-iam/test";
    private static final String APPLIANCE_URL_PROPERTY = "CONJUR_APPLIANCE_URL";


    @Test
    public void testLogin() {
        Conjur conjur = new Conjur();

        // The Conjur object is returned with an Authn client logged in
        Assert.assertNotNull(conjur);
    }

    @Test
    public void testAddSecretAndRetrieveSecret() {
        Conjur conjur = new Conjur();

        conjur.variables().addSecret(VARIABLE_KEY, VARIABLE_VALUE);

        String retrievedSecret = conjur.variables().retrieveSecret(VARIABLE_KEY);

        Assert.assertEquals(retrievedSecret, VARIABLE_VALUE);
    }

    @Test
    public void testSetVariableWithoutVariableInPolicy() {
        expectedException.expect(WebApplicationException.class);
        expectedException.expectMessage(NOT_FOUND_STATUS_CODE);

        Conjur conjur = new Conjur();

        conjur.variables().addSecret(NON_EXISTING_VARIABLE_KEY, VARIABLE_VALUE);
    }

    @Test
    public void testLogonWithAlterativeAuthenticator() {
        expectedException.expect(ProcessingException.class);
        expectedException.expectMessage(UNAUTHORIZED_STATUS_CODE);

        String authnUrl = System.getProperty(APPLIANCE_URL_PROPERTY) + ALTERNATIVE_AUTHN_ENDPOINT;

        Conjur conjur = new Conjur(ALTERNATIVE_USERNAME, ALTERNATIVE_API_KEY, authnUrl);
        conjur.variables().retrieveSecret(VARIABLE_KEY);
    }


    @Rule
    public ExpectedException expectedException = ExpectedException.none();
}
