package net.conjur.api;

import net.conjur.api.authn.AuthnProvider;
import net.conjur.util.Args;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 *
 */
public class Users extends Resource {
    private WebTarget users;
    private WebTarget roles;

    Users(AuthnProvider authn, Endpoints endpoints) {
        super(authn, endpoints);
        init();
    }

    Users(Resource relative){
        super(relative);
        init();
    }

    public User create(final String username, final String password){
        return create(new Form("login", username).param("password", password));
    }

    public User create(final String username){
        return create(new Form("login", username));
    }

    public boolean exists(final String username){
        Response response = roles.resolveTemplate("id", roleId(username)).request().head();
        if(response.getStatusInfo() == Response.Status.NOT_FOUND)
            return false;
        if(response.getStatusInfo() == Response.Status.FORBIDDEN)
            return true;
        if(response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL)
            return true;
        // should throw?
        response.readEntity(String.class);
        throw new IllegalStateException("should be unreachable");
    }

    private String roleId(final String username){
        return "user:" + username;
    }

    private void init(){
        users = target(getEndpoints().getDirectoryUri()).path("users");
        roles = target(getEndpoints().getAuthzUri()).path("roles/{id}");
    }

    private WebTarget users(){
        return users;
    }

    private User create(Form params){
        return users().request(MediaType.APPLICATION_JSON).post(Entity.form(params), User.class);
    }
}
