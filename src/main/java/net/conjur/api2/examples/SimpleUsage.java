package net.conjur.api2.examples;

import net.conjur.api2.Conjur;
import org.junit.Assert;

public class SimpleUsage {

    private static final String SECRET_KEY = "test-secret";
    private static final String SECRET_VALUE = "test-secret-value";

    public static void main(String[] argv) {

        // Get an instance of Conjur in order to set and retrieve secrets from the conjur vault
        Conjur conjur = Conjur.getInstance();

        // set a secret
        conjur.setSecret(SECRET_KEY, SECRET_VALUE);

        // retrieve the secret from the conjur vault
        String retrievedSecret = conjur.getSecret(SECRET_KEY);

        // verify that the retrieved secret is the expected one
        Assert.assertTrue(retrievedSecret.equals(SECRET_VALUE));
    }
}
