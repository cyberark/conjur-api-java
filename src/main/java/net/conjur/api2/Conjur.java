package net.conjur.api2;


/**
 * Entry point for the Conjur API client.
 */
public class Conjur {

    private static Conjur instance;

    private Credentials credentials;
    private String token;

    private Conjur(){
        credentials = Credentials.fromSystemProperties();
    }

    /**
     * @return an instance of Conjur, ready for storing and retrieving secrets
     */
    public static Conjur getInstance() {
        if (instance == null) {
            instance = new Conjur();
        }

        if (!isTokenValid()) {
            login();
            authenticate();
        }

        return instance;
    }

    private static void login() {
        // use hostname, apiKey, url & accountName from credentials object (credentials.hostname etc.)

        // token=$(curl --user admin:3j9v7d52s8q2jz35pvz4he01c61ghsrfj3xm9s893ay1vks60hhrw http://possum:3000/authn/test/login)
    }

    private static void authenticate() {
        // use token (retrieved in login method)
        // use hostname, apiKey, url & accountName from credentials object (credentials.hostname etc.)

        // response=$(curl --data "$login_token" http://possum:3000/authn/test/admin/authenticate)
        // token=$(echo -n $response | base64 | tr -d '\r\n')
    }

    private static boolean isTokenValid() {
        // check with server if the token received in last login is still valid
        return true;
    }

    public String getSecret(String secretKey) {
        // use token (retrieved in login method)

        // curl -H "Authorization: Token token=\"$token\"" http://possum:3000/secrets/test/variable/test-secret
        return null;
    }

    public void setSecret(String secretKey, String secretValue){
        // curl -H "Authorization: Token token=\"$token\"" --data "super-secret" http://possum:3000/secrets/test/variable/test-secret
    }
}
