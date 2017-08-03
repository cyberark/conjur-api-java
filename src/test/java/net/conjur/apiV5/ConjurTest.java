package net.conjur.apiV5;

import net.conjur.apiV5.clients.ConjurException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

/**
 * Test for the Conjur class
 *
 * Before running this test, verify that:
 *  - Conjur CE is running, healthy and accessible
 *  - A Policy that provides permission for this application to access a secret is loaded
 *  - This policy has an account and a variable named 'testSecret' related to that account
 *  - The following system properties are loaded:
 *      * net.conjur.api.account=accountName
 *      * net.conjur.api.credentials=username:apiKey
 *      * net.conjur.api.url=http://conjur:3000
 */
public class ConjurTest {

    private static final String VARIABLE_KEY = "testSecret";
    private static final String VARIABLE_VALUE = "testSecretValue";
    private static final String NON_EXISTING_VARIABLE_KEY = UUID.randomUUID().toString();

    public ConjurTest() {
    }

    @Test
    public void testLoginAndAuthenticate() {
        // if getInstance returns a Conjur object then the login and authentication passed
        Conjur conjur = Conjur.getInstance();
        Assert.assertNotNull(conjur);
    }

    @Test
    public void testAddSecretAndRetrieveSecret() {
        // Get an instance of Conjur and set a secret
        Conjur.getInstance().addSecret(VARIABLE_KEY, VARIABLE_VALUE);

        // retrieve the secret
        String retrievedSecret = Conjur.getInstance().retrieveSecret(VARIABLE_KEY);

        // verify that the retrieved secret is the expected one
        Assert.assertEquals(retrievedSecret, VARIABLE_VALUE);
    }

    @Test
    public void testSetVariableWithoutVariableInPolicy() {
        expectedException.expect(ConjurException.class);
        expectedException.expectMessage("not found in this account");

        Conjur.getInstance().addSecret(NON_EXISTING_VARIABLE_KEY, VARIABLE_VALUE);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
}