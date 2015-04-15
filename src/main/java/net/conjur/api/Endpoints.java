package net.conjur.api;


import net.conjur.util.Args;

import javax.ws.rs.core.UriBuilder;
import java.io.Serializable;
import java.net.URI;
import java.util.Properties;

/**
 * An <code>Endpoints</code> instance provides endpoint URIs for the various conjur services.
 * This is an abstract base class allowing users to provide their own endpoints, for example when
 * the conjur services are being hosted on premises.
 *
 */
public class Endpoints implements Serializable {
    public static final Endpoints DEFAULT_ENDPOINTS = fromSystemProperties();
    private static final String DEFAULT_STACK = "v4";

    private final URI authnUri;
    private final URI authzUri;
    private final URI directoryUri;

    public Endpoints(final URI authnUri,
                     final URI authzUri,
                     final URI directoryUri){
        this.authnUri = Args.notNull(authnUri, "authnUri");
        this.authzUri = Args.notNull(authzUri, "authzUri");
        this.directoryUri = Args.notNull(directoryUri, "directoryUri");
    }

    public Endpoints(String authnUri, String authzUri, String directoryUri){
        // My kingdom for varargs and map!
        this(URI.create(authnUri), URI.create(authzUri), URI.create(directoryUri));
    }

    public URI getAuthnUri(){ return authnUri; }
    public URI getDirectoryUri(){ return directoryUri; }
    public URI getAuthzUri(){ return authzUri; }

    /**
     * @deprecated use the constructor instead
     */
    public static Endpoints of(String authnUri, String authzUri, String directoryUri){
        return new Endpoints(authnUri,authzUri,directoryUri);
    }

    /**
     * @deprecated use the constructor instead
     */
    public static Endpoints of(URI authnUri, URI authzUri, URI directoryUri){
        return new Endpoints(authnUri, authzUri, directoryUri);
    }

    /**
     * Create an Endpoints instance from an appliance URL.  An appliance url
     * must be of the form <code>"https://hostname/api"</code>, for example,
     * <code>"https://conjur.companyname.com/api"</code>.  This method will verify that
     * this is the case and throw an <code>IllegalArgumentException</code> if it's not.
     * @param applianceUri the appliance URI
     * @return Endpoints for the appliance.
     */
    public static Endpoints getApplianceEndpoints(URI applianceUri){
        final String scheme = applianceUri.getScheme();

        if(scheme == null || !scheme.startsWith("https")){
            throw new IllegalArgumentException("Invalid applianceUri: " + applianceUri + ": expected https scheme");
        }

        final  String path = applianceUri.getPath();
        if(path == null || !path.equals("/api")){
            throw new IllegalArgumentException("Invalid applianceUri: " + applianceUri + ": expected path to be /api");
        }

        final URI authnUri = UriBuilder.fromUri(applianceUri).segment("authn").build();
        final URI authzUri = UriBuilder.fromUri(applianceUri).segment("authz").build();

        return new Endpoints(authnUri, authzUri, applianceUri);
    }

    public static Endpoints getApplianceEndpoints(String applianceUri){
        return getApplianceEndpoints(URI.create(applianceUri));
    }

    /**
     * Return endpoints for the specified hosted Conjur stack and account.
     * @param stack the Conjur stack
     * @param account the Conjur account
     * @return
     */
    public static Endpoints getHostedEndpoints(String stack, String account){
        return of(
           getHostedServiceUri("authn", account),
           getHostedServiceUri("authz", stack, account),
           getHostedServiceUri("core", account)
        );
    }
    
    /**
     * Return endpoints for the specified hosted Conjur account.  If you don't know
     * what stack to use, you should use this method to get an instance.
     * @param account the Conjur account
     * @return
     */
    public static Endpoints getHostedEndpoints(String account){
        return getHostedEndpoints(DEFAULT_STACK, account);
    }


    /**
     * @deprecated
     * @return
     */
    public static Endpoints fromSystemProperties(){
        return fromProperties(System.getProperties());
    }

    /**
     * @deprecated
     * @param properties
     * @return
     */
    public static Endpoints fromProperties(Properties properties){
    	// when maven sets system properties from environment variables
    	// it will set them to "null" (as a string) if they are not present.
    	String stack = properties.getProperty("net.conjur.api.stack");
    	if(stack == null || "null".equals(stack)){
    		stack = DEFAULT_STACK;
    	}
    	String account = properties.getProperty("net.conjur.api.account");
    	if(account == null || "null".equals(account)){
    		account = "sandbox";
    	}
    	return getHostedEndpoints(stack, account);
    }

    /**
     * Return default Conjur endpoints, either those set by {@link Endpoints#setDefault(Endpoints)} 
     * or hosted endpoints for the sandbox account on the default stack.
     */
    public static Endpoints getDefault(){
        return defaultEndpoints == null ? DEFAULT_ENDPOINTS : defaultEndpoints;
    }

    /**
     * Set the endpoints to return from {@link Endpoints#getDefault()}
     * @param defaultEndpoints
     */
    public static void setDefault(Endpoints defaultEndpoints){
        Endpoints.defaultEndpoints = defaultEndpoints;
    }

    private static URI getHostedServiceUri(String service, String name){ return getHostedServiceUri(service, name, ""); }

    private static URI getHostedServiceUri(String service, String name, String path){
        // returns a uri like https://{service}-{name}-conjur.herokuapp.com/{path}
        return URI.create(String.format("https://%s-%s-conjur.herokuapp.com/%s", service, name, path));
    }

    private static Endpoints defaultEndpoints = DEFAULT_ENDPOINTS;


    @Override
    public String toString() {
        return "Endpoints{" +
                "authnUri=" + authnUri +
                ", authzUri=" + authzUri +
                ", directoryUri=" + directoryUri +
                '}';
    }
}
