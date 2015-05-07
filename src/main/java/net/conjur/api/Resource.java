package net.conjur.api;

import net.conjur.api.authn.AuthnProvider;
import net.conjur.util.HostNameVerification;
import net.conjur.util.rs.JsonBodyReader;
import net.conjur.util.rs.TokenAuthFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;

/**
 * Base class for Conjur service clients.
 * 
 * <p>A {@code Resource} is configured with an authentication provider thatm
 * produces Conjur authentication tokens as needed, and endpoints for the Conjur
 * services</p>
 */
public class Resource {
    private static final Log LOG = LogFactory.getLog("Request logger");
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

    Resource setRelative(Resource relative){
        this.authn = relative.getAuthn();
        this.endpoints = relative.getEndpoints();
        client = createClient();
        return this;
    }

    Resource(){
        /* private ctor, you must call setRelative after using it. */
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
                .register(new JsonBodyReader());
        HostNameVerification.getInstance().updateClientBuilder(builder);

        return builder.build();
    }

}