package net.conjur.api;

import net.conjur.api.authn.AuthnProvider;
import net.conjur.api.authn.TokenAuthFilter;
import net.conjur.util.logging.LogFilter;
import org.codehaus.jackson.map.InjectableValues;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.ext.ContextResolver;
import java.net.URI;

/**
 * Base class for Conjur service clients.
 * 
 * <p>A {@code Resource} is configured with an authentication provider that 
 * produces Conjur authentication tokens as needed, and endpoints for the Conjur
 * services</p>
 */
public class Resource {
    private AuthnProvider authn;
    private Endpoints endpoints;
    private Client client;

    public Resource(AuthnProvider authn, Endpoints endpoints){
        this.authn = authn;
        this.endpoints = endpoints;
        client = createClient();
    }

    /**
     * Create a resource using the same {@code AuthnProvider} and {@code Endpoints}
     * as {@code relative}.
     * @param relative the {@code Resource} from which to take the authentication provider 
     * and endpoints for the new {@code Resource}
     */
    public Resource(Resource relative){
        this(relative.getAuthn(), relative.getEndpoints());
    }

    /**
     * @return the authentication provider for this resource
     */
    public AuthnProvider getAuthn() {
        return authn;
    }

    /**
     * @return the endpoints for this resource
     */
    protected Endpoints getEndpoints() {
        return endpoints;
    }

    /**
     * Create a {@code WebTarget} for the given uri.
     * @param uri
     * @return
     */
    protected WebTarget target(URI uri){
        return client.target(uri);
    }

    /**
     * @return The {@code Client} used by this {@code Resource}
     */
    protected Client client(){
        return client;
    }

    /**
     * Subclasses may override this to provide custom configuration for their client.  The
     * {@code Client} returned will be returned by {@link #client()}.
     * @return
     */
    protected Client createClient(){
        ClientBuilder builder = ClientBuilder.newBuilder()
                .register(new TokenAuthFilter(authn))
                .register(JacksonFeature.class)
                .register(contextResolver);
        if(requestLoggingEnabled()){
            builder.register(new LogFilter());
        }
        return builder.build();
    }

    // TODO this is a stupid hack
    private static final boolean requestLoggingEnabled(){
        final String prop = System.getProperty("net.conjur.api.resource.requestLogging");
        if(prop != null && prop.toLowerCase().equals("true")){
            return true;
        }
        return false;
    }

    private final ContextResolver<ObjectMapper> contextResolver = new ContextResolver<ObjectMapper>() {
        public ObjectMapper getContext(Class<?> type) {
            final InjectableValues values = new InjectableValues.Std()
                    .addValue(Resource.class, Resource.this);
            return new ObjectMapper().setInjectableValues(values);
        }
    };
}