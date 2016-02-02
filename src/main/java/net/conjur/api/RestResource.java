package net.conjur.api;

import com.google.gson.annotations.SerializedName;
import net.conjur.api.authn.AuthnProvider;
import net.conjur.util.HostNameVerification;
import net.conjur.util.rs.JsonBodyReader;
import net.conjur.util.rs.JsonReadable;
import net.conjur.util.rs.TokenAuthFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.URI;

/**
 * Base class for Conjur service clients.
 * 
 * <p>A {@code RestResource} is configured with an authentication provider thatm
 * produces Conjur authentication tokens as needed, and endpoints for the Conjur
 * services</p>
 */
public class RestResource {
    private static final Log LOG = LogFactory.getLog("Request logger");
    private AuthnProvider authn;
    private Endpoints endpoints;
    private Client client;
    private String account =  null;

    public RestResource(AuthnProvider authn, Endpoints endpoints){
        this.authn = authn;
        this.endpoints = endpoints;
        client = createClient();
    }

    /**
     * Create a resource using the same {@code AuthnProvider} and {@code Endpoints}
     * as {@code relative}.
     * @param relative the {@code RestResource} from which to take the authentication provider
     * and endpoints for the new {@code RestResource}
     */
    public RestResource(RestResource relative){
        this(relative.getAuthn(), relative.getEndpoints());
    }

    /**
     * @return the authentication provider for this resource
     */
    public AuthnProvider getAuthn() {
        return authn;
    }

    RestResource setRelative(RestResource relative){
        this.authn = relative.getAuthn();
        this.endpoints = relative.getEndpoints();
        client = createClient();
        return this;
    }

    RestResource(){
        /* private ctor, you must call setRelative after using it. */
    }

    /**
     * Get the account for this instance.  This method queries the Conjur appliances /api/info route,
     * then caches the result.
     *
     * @return the Conjur account
     */
    public String getAccount(){
        if(account == null){
            account = fetchAccount();
        }
        return account;
    }

    public ConjurIdentifier resolveId(String id){
        return ConjurIdentifier.parse(id, getAccount());
    }

    protected String usernameToRoleId(String username){
        String kind = "user";

        if(username.startsWith("host/")){
            kind = "host";
            username = username.substring(5);
        }

        return kind + ":" + username;
    }


    private String fetchAccount(){
        final WebTarget target = target(getEndpoints().getDirectoryUri()).path("info");
        return target.request(MediaType.APPLICATION_JSON_TYPE).get(Info.class).account;
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
     * @return The {@code Client} used by this {@code RestResource}
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


    @JsonReadable
    private static class Info {
        @SerializedName("account")
        public String account;
    }
}