package net.conjur.apiV5;

public class Credentials {
    private static final String CREDENTIALS_PROPERTY = "CONJUR_CREDENTIALS";

    private String username;
    private String password;

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static Credentials fromSystemProperties(){
        try {
            String credentials = System.getProperty(CREDENTIALS_PROPERTY);

            // Assuming credentials are written in the systemParams in this way: "username:password"
            String[] parts = credentials.split(":");

            return new Credentials(parts[0], parts[1]);
        } catch (NullPointerException e) {
            throw new ConjurException(String.format("System property %s is not set in the pattern username:password", CREDENTIALS_PROPERTY));
        }

    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
