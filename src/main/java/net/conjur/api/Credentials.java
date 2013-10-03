package net.conjur.api;

import net.conjur.util.Args;

import static net.conjur.util.Args.*;

public class Credentials {
    public static final String CREDENTIALS_PROPERTY = "net.conjur.api.credentials";

    private String username;
    private String password;

    public Credentials(String username, String password) {
        this.username = Args.notBlank(username, "Username");
        this.password = Args.notBlank(password, "Password");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String toString(){
        return username + ":" + password;
    }

    public static Credentials fromSystemProperties(){
        return fromString(System.getProperty(CREDENTIALS_PROPERTY));
    }

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
