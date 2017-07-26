package net.conjur.api2;

public class Credentials {
    private static final String CREDENTIALS_PROPERTY = "net.conjur.api.credentials";

    private String hostname;
    private String apiKey;
    private String url;
    // todo: add CA Cert?

    public Credentials(String hostname, String apiKey, String url) {
        this.hostname = hostname;
        this.apiKey = apiKey;
        this.url = url;
    }

    public static Credentials fromSystemProperties(){
        String credentials = System.getProperty(CREDENTIALS_PROPERTY);
        String[] parts = credentials.split(":"); // Assuming credentials are written in the systemParams in this way: "hostname:apiKey:url"
        return new Credentials(parts[0], parts[1], parts[2]);
    }

    public String getHostname() {
        return hostname;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getUrl() {
        return url;
    }

}
