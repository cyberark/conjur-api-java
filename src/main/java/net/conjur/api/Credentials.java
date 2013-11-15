package net.conjur.api;

import net.conjur.util.Args;

import static net.conjur.util.Args.*;

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
    public static final String CREDENTIALS_PROPERTY = "net.conjur.api.credentials";

    private String username;
    private String password;

    /**
     * @param username the username/login for this Conjur identity
     * @param password the password or api key for this Conjur identity
     */
    public Credentials(String username, String password) {
        this.username = Args.notBlank(username, "Username");
        this.password = Args.notBlank(password, "Password");
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

    /**
     * Creates a Credentials instance by parsing a string stored in the system
     * property {@link Credentials#CREDENTIALS_PROPERTY}.
     * @return the credentials stored in the system property.
     * @throws IllegalArgumentException if the string is not in the format expected by {@link Credentials#fromString(String)}
     * @throws NullPointerException if the 
     */
    public static Credentials fromSystemProperties(){
        return fromString(System.getProperty(CREDENTIALS_PROPERTY));
    }

    /**
     * Parses a string like <code>"username:password"</code>
     * @param string the string to parse
     * @return the parsed credentials
     * @throws IllegalArgumentException if the string is not in the above format.
     */
    public static Credentials fromString(String string){
        String[] parts = notNull(string, "Credentials").split(":", 2);
        if(parts.length != 2)
            throw new IllegalArgumentException("Invalid credential string \"" + string + "\"");
        return new Credentials(parts[0], parts[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Credentials)) return false;

        Credentials that = (Credentials) o;

        if (!password.equals(that.password)) return false;
        if (!username.equals(that.username)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }
}
