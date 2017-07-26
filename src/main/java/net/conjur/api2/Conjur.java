package net.conjur.api2;


/**
 *
 */
public class Conjur {

    private static Conjur instance;

    private Credentials credentials;

    private Conjur(){
        credentials = Credentials.fromSystemProperties();
    }

    public static Conjur getInstance() {
        if (instance == null) {
            instance = new Conjur();
        }

        // login

        // authenticate

        return instance;
    }

    public String getSecret() {
        return "changeit";
    }
}
