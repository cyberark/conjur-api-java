package net.conjur.apiV5.examples;

import net.conjur.apiV5.Conjur;
import org.junit.Assert;

public class SimpleUsage {

    private static final String VARIABLE_KEY = "testSecret";
    private static final String VARIABLE_VALUE = "testSecretValue";

    public static void main(String[] argv) {

        // Get an instance of Conjur and set a secret
        Conjur.getInstance().setVariable(VARIABLE_KEY, VARIABLE_VALUE);

        // retrieve the secret
        String retrievedSecret = Conjur.getInstance().getVariable(VARIABLE_KEY);

        // verify that the retrieved secret is the expected one
        Assert.assertTrue(retrievedSecret.equals(VARIABLE_VALUE));
    }
}
