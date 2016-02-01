package net.conjur.api;

import com.google.gson.annotations.SerializedName;
import net.conjur.api.authn.AuthnClient;
import net.conjur.api.authn.AuthnProvider;
import net.conjur.util.Args;
import net.conjur.util.JsonSupport;
import net.conjur.util.rs.JsonReadable;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Entry point for the Conjur API client.
 * 
 * <p>This class provides access to various Conjur resources, using
 * a particular set of endpoints and authorization or credentials</p>
 */
public class Conjur extends RestResource {
    private Users users;
    private Variables variables;
    // sigh
    private String account;

    /**
     * @param authn used to authenticate requests to Conjur services
     * @param endpoints locates Conjur services
     */
    public Conjur(AuthnProvider authn, Endpoints endpoints) {
        super(authn, endpoints);
        init();
    }

    /**
     * @deprecated  tthis constr
     * Create a Conjur instance that uses the given AuthnProvider and 
     * {@link Endpoints#getDefault()}.
     * @param authn used to authenticate requests to Conjur services
     */
    public Conjur(AuthnProvider authn){
        this(authn, Endpoints.getDefault());
    }

    /**
     * Create a Conjur instance that uses an AuthnClient constructed with
     * the given credentials and the given endpoints
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     * @param endpoints locates Conjur services
     */
    public Conjur(String username, String password, Endpoints endpoints){
        this(new AuthnClient(username, password, endpoints), endpoints);
    }

    /**
     * Create a Conjur instance that uses an AuthnClient constructed with
     * the given credentials and the default endpoints. 
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     */
    public Conjur(String username, String password){
        this(new AuthnClient(username, password));
    }

    /**
     * Create a Conjur instance that uses an AuthnClient constructed with
     * the given credentials and the default endpoints.
     * @param credentials the conjur identity to authenticate as
     */
    public Conjur(Credentials credentials){
        this(credentials.getUsername(), credentials.getPassword());
    }

    /**
     * Create a Conjur instance that uses an AuthnClient constructed with
     * the given credentials and the default endpoints.
     * @param credentials the conjur identity to authenticate as
     */
    public Conjur(Credentials credentials, Endpoints endpoints){
        this(credentials.getUsername(), credentials.getPassword(), endpoints);
    }

    /**
     * Get a Users instance configured with the same parameters as this instance.
     * @return the users instance
     */
    public Users users(){
        return users;
    }

    /**
     * Get a Variables instance configured with the same parameters as this instance.
     * @return the variables instance
     */
    public Variables variables(){
        return variables;
    }

    /**
     * Get the authentication provider for this instance.
     * @return the authentication provider
     */
    public AuthnProvider getAuthn(){
        return super.getAuthn();
    }

    /**
     * Check whether the authenticated role for this API instance has
     * permission {@code privilege} on {@code resource}.  Resource should
     * be a string like "account:kind:identifier" or "kind:identifier".
     *
     * @param resource identifier for the resource
     * @param privilege name of the permission (e.g. "read", "update")
     * @return whether the authenticated role for this instance has the privilege
     *  on the given resource.
     */
    public boolean isPermitted(String resource, String privilege){
        return isPermitted(usernameToRoleId(getAuthn().getUsername()),
                resource, privilege);
    }

    /**
     * Check whether the given role has permission {@code privilege} on {@code resource}.
     * Resource and role should be a string like "account:kind:identifier" or "kind:identifier".
     *
     * @param role identifier of the role
     * @param resource identifier for the resource
     * @param privilege name of the permission (e.g. "read", "update")
     * @return whether the authenticated role for this instance has the privilege
     *  on the given resource.
     */
    public boolean isPermitted(String role, String resource, String privilege){
        Args.notNull(privilege, "privilege");

        ConjurIdentifier roleId = ConjurIdentifier.parse(role, getAccount());
        ConjurIdentifier resourceId = ConjurIdentifier.parse(resource, getAccount());

        WebTarget check = getRoleTarget(roleId)
                .queryParam("check", "true")
                .queryParam("resource_id", resourceId.toString())
                .queryParam("privilege", privilege);

        try{
            check.request().get();
        }catch(ForbiddenException e){
            return false;
        }catch(NotFoundException e){
            return false;
        }

        return true;
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

    private WebTarget getRoleTarget(ConjurIdentifier roleId){
        return target(getEndpoints().getAuthzUri())
                .path(roleId.getAccount())
                .path("roles")
                .path(roleId.getKind())
                .path(roleId.getId());
    }

    private String fetchAccount(){
        final WebTarget target = target(getEndpoints().getDirectoryUri()).path("info");
        return target.request(MediaType.APPLICATION_JSON_TYPE).get(Info.class).account;
    }

    private String usernameToRoleId(String username){
        String kind = "user";

        if(username.startsWith("host/")){
            kind = "host";
            username = username.substring(5);
        }

        return kind + ":" + username;
    }

    private void init(){
        users = new Users(this);
        variables = new Variables(this);
    }

    @JsonReadable
    private static class Info {
        @SerializedName("account")
        public String account;
    }


}
