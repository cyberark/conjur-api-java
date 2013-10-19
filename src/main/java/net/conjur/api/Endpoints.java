package net.conjur.api;


import net.conjur.util.Args;

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

    private Endpoints(final URI authnUri,
                     final URI authzUri,
                     final URI directoryUri){
        this.authnUri = Args.notNull(authnUri, "authnUri");
        this.authzUri = Args.notNull(authzUri, "authzUri");
        this.directoryUri = Args.notNull(directoryUri, "directoryUri");
    }

    private Endpoints(String authnUri, String authzUri, String directoryUri){
        // My kingdom for varargs and map!
        this(URI.create(authnUri), URI.create(authzUri), URI.create(directoryUri));
    }
    public URI getAuthnUri(){ return authnUri; }
    public URI getDirectoryUri(){ return directoryUri; }
    public URI getAuthzUri(){ return authzUri; };

    public static Endpoints of(String authnUri, String authzUri, String directoryUri){
        return new Endpoints(authnUri,authzUri,directoryUri);
    }

    public static Endpoints of(URI authnUri, URI authzUri, URI directoryUri){
        return new Endpoints(authnUri, authzUri, directoryUri);
    }

    public static Endpoints getHostedEndpoints(String stack, String account){
        return of(
           getHostedServiceUri("authn", account),
           getHostedServiceUri("authz", stack, account),
           getHostedServiceUri("core", account)
        );
    }

    public static Endpoints getHostedEndpoints(String account){
        return getHostedEndpoints(DEFAULT_STACK, account);
    }


    public static Endpoints fromSystemProperties(){
        return fromProperties(System.getProperties());
    }

    public static Endpoints fromProperties(Properties properties){
        return getHostedEndpoints(
                properties.getProperty("net.conjur.api.stack", DEFAULT_STACK),
                properties.getProperty("net.conjur.api.account", "sandbox")
        );
    }

    public static Endpoints getDefault(){
        return defaultEndpoints == null ? DEFAULT_ENDPOINTS : defaultEndpoints;
    }

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
