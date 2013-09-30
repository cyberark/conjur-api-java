package net.conjur.api;

import org.apache.http.util.Args;

import java.io.Serializable;

/**
 * Credentials for the conjur services
 */
public class Credentials implements Serializable {
    private static final long serialVersionUID = -5437299376222011036L;

    private String login;
    private String apiKey;
    private String password;

    public String getLogin() {
        return login;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getPassword() {
        return password;
    }

    public Credentials withApiKey(String apiKey) {
        return new Credentials(login, password, apiKey);
    }

    Credentials(String login, String password, String apiKey){
        this.login = Args.notNull(login, "login");
        if(password == null && apiKey == null)
            throw new IllegalArgumentException("either password or apiKey must be non-null");
        this.password = password;
        this.apiKey = apiKey;
    }

    public static Credentials fromPassword(String login, String password){
        return new Credentials(login, password, null);

    }

    public static Credentials fromKey(String login, String apiKey){
        return new Credentials(login, null, apiKey);
    }
}
