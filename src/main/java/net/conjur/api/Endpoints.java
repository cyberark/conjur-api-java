package net.conjur.api;

public class Endpoints {
	public static final int AUTHN_PORT = 5000;
	public static final int AUTHZ_PORT = 5100;
	public static final int DIRECTORY_PORT = 5200;
	
	private final String environment;
	
	private final String stack;
	
	private Endpoints(String environment, String stack){
		this.environment = environment;
		this.stack = stack;
	}
	
	public String authn(String account){
		return Endpoints.authnUrl(environment, stack, account);
	}
	
	public String authz(String account){
		return Endpoints.authzUrl(environment, stack, account);
	}
	
	public String directory(String account){
		return Endpoints.directoryUrl(environment, stack, account);
	}
	
	// I can't think of a decent name for this method...
	public static Endpoints of(String environment, String stack){
		return new Endpoints(environment, stack);
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
}
