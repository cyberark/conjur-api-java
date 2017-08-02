package net.conjur.apiV5;

public class Credentials {
    private static final String CREDENTIALS_PROPERTY = "net.conjur.api.credentials";

    private String username;
    private String password;

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static Credentials fromSystemProperties(){
        String credentials = System.getProperty(CREDENTIALS_PROPERTY);

        // Assuming credentials are written in the systemParams in this way: "username:password"
        String[] parts = credentials.split(":");

        return new Credentials(parts[0], parts[1]);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
