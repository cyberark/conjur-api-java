package net.conjur.api;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.WebApplicationException;
import java.util.UUID;

/**
 * Test for the Conjur class
 *
 * Before running this test, verify that:
 *  - Conjur CE is running, healthy and accessible
 *  - A Policy that provides permission for this application to access a secret is loaded
 *  - This policy has an account and a variable named 'testSecret' related to that account
 *  - The following system properties are loaded:
 *      * CONJUR_ACCOUNT=accountName
 *      * CONJUR_CREDENTIALS=username:apiKey
 *      * CONJUR_APPLIANCE_URL=http://conjur
 */
public class ConjurTest {

    private static final String VARIABLE_KEY = "testSecret";
    private static final String VARIABLE_VALUE = "testSecretValue";
    private static final String NON_EXISTING_VARIABLE_KEY = UUID.randomUUID().toString();
    private static final String NOT_FOUND_STATUS_CODE = "404";

    public ConjurTest() {
    }

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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
}