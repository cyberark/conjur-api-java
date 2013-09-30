package net.conjur.api;


import java.net.URI;

public class Endpoints {
    public static final String ENVIRONMENT_PROPERTY = "conjur.net.environment";
    public static final String STACK_PROPERTY = "conjur.net.endpoints.stack";
    public static final String ACCOUNT_PROPERTY = "conjur.net.endpoints.account";
    public static final String DEFAULT_ENVIRONMENT = "production";
    public static final String DEFAULT_STACK = "v3";
    public static final String DEFAULT_ACCOUNT = "sandbox";

	public static final int AUTHN_PORT = 5000;
	public static final int AUTHZ_PORT = 5100;
	public static final int DIRECTORY_PORT = 5200;
	
	private final String environment;
	
	private final String stack;
	
	private final String account;
	
	public Endpoints(String environment, String stack, String account){
		this.environment = environment;
		this.stack = stack;
		this.account = account;
	}
	
	public Endpoints(String stack, String account){
		this("production", stack, account);
	}
	
	
	
	public String authn(){
		return Endpoints.authnUrl(environment, stack, account);
	}
	
	public String authz(){
		return Endpoints.authzUrl(environment, stack, account);
	}
	
	public String directory(){
		return Endpoints.directoryUrl(environment, stack, account);
	}
	
	public static String authnUrl(String environment, String stack, String account){
		if(useLocalhost(environment)){
			return localEndpoint(AUTHN_PORT);
		}
		return realEndpoint("authn", account);
	}
	
	public static String authzUrl(String environment, String stack, String account){
		if(useLocalhost(environment))
			return localEndpoint(AUTHZ_PORT);
		return realEndpoint("authz", stack);
	}
	
	public static String directoryUrl(String environment, String stack, String account){
		if(useLocalhost(environment))
			return localEndpoint(DIRECTORY_PORT);
		return realEndpoint("core", account);
	}

	private static boolean useLocalhost(String environment){
		if(environment == null)
			throw new NullPointerException();
		return "test".equals(environment) || "development".equals(environment);
	}
	
	private static String localEndpoint(int port){
		return String.format("http://localhost:%d", port);
	}
	
	private static String realEndpoint(String service, String name){
		return String.format("https://%s-%s-conjur.herokuapp.com", service, name);
	}

    public URI authnUri() {
        return URI.create(authn());
    }

    public URI directoryUri() {
        return URI.create(directory());
    }

    // TODO make this stuff thread safe!
    private static Endpoints defaultEndpoints;
    private static final Endpoints systemEndpoints = createSystemEndpoints();

    /**
     * Create endpoints configured from system properties or set globally.
     * @warning Not thread safe
     * @return
     */
    public static Endpoints getDefault() {
        return defaultEndpoints == null ? getSystemEndpoints() : defaultEndpoints;
    }

    /**
     * Set global default endpoints
     * @warning Not thread safe
     * @param endpoints
     */
    public static void setDefault(Endpoints endpoints){
        defaultEndpoints = endpoints;
    }

    /**
     * Get endpoints configured from system properties
     * @return
     */
    public static Endpoints getSystemEndpoints(){
        return systemEndpoints;
    }

    public static Endpoints createSystemEndpoints(){
        String environment = System.getProperty(ENVIRONMENT_PROPERTY, DEFAULT_ENVIRONMENT);
        String stack = System.getProperty(STACK_PROPERTY, DEFAULT_STACK);
        String account = System.getProperty(ACCOUNT_PROPERTY, DEFAULT_ACCOUNT);
        return new Endpoints(environment, stack, account);
    }
}
