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
    private static final String CREDENTIALS_PROPERTY = "CONJUR_CREDENTIALS";

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
     * Creates a Credentials instance by parsing a string stored in the system
     * property {@link Credentials#CREDENTIALS_PROPERTY}.
     * @return the credentials stored in the system property.
     * @throws IllegalArgumentException if the string is not in the format "username:password"
     * @throws NullPointerException if the system property {@link Credentials#CREDENTIALS_PROPERTY} does not exist
     */
    public static Credentials fromSystemProperties(){
        String credentials = System.getProperty(CREDENTIALS_PROPERTY);

        // Assuming credentials are written in the systemParams in this way: "username:password"
        String[] parts = credentials.split(":");

        if(parts.length != 2) {
            throw new IllegalArgumentException("Invalid credential string \"" + credentials + "\"");
        }

        return new Credentials(parts[0], parts[1]);
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
