package net.conjur.api;

/**
 * Stores credentials for a Conjur identity.
 *
 * <p>A Conjur identity consists of a username and a password.  The password
 * may either be a user's password or the api key of a user or host.  These
 * cases are treated identically by the Conjur authentication service.</p>
 *
 * <p>Credentials supports hashCode and equals and can be used as keys, for
 * example for caching.</p>
 */
public class Credentials {
    private static final String CONJUR_AUTHN_LOGIN_PROPERTY = "CONJUR_AUTHN_LOGIN";
    private static final String CONJUR_AUTHN_API_KEY_PROPERTY = "CONJUR_AUTHN_API_KEY";

    private String username;
    private String password;

    /**
     * @param username the username/login for this Conjur identity
     * @param password the password or api key for this Conjur identity
     */
    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Creates a Credentials instance from the system properties {@link Credentials#CONJUR_AUTHN_LOGIN_PROPERTY} & {@link Credentials#CONJUR_AUTHN_API_KEY_PROPERTY}
     * @return the credentials stored in the system property.
     */
    public static Credentials fromSystemProperties(){
        String login = System.getProperty(CONJUR_AUTHN_LOGIN_PROPERTY);
        String apiKey = System.getProperty(CONJUR_AUTHN_API_KEY_PROPERTY);

        return new Credentials(login, apiKey);
    }

    /**
     * @return the username/login for this Conjur identity
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the api key or password for this Conjur identity
     */
    public String getPassword() {
        return password;
    }

    public String toString(){
        return username + ":" + password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof net.conjur.api.Credentials)) return false;

        net.conjur.api.Credentials that = (net.conjur.api.Credentials) o;

        if (!password.equals(that.getPassword())) return false;
        if (!username.equals(that.getUsername())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }
}
