package net.conjur.api;

import net.conjur.api.authn.AuthnProvider;
import net.conjur.util.Args;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static net.conjur.util.EncodeUriComponent.encodeUriComponent;

/**
 * Resource representing Conjur user services.
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
        try{
            // we need to read it as a string to make jersey throw an exception, I believe.
            roles.path(encodeUriComponent(username)).request().get(String.class);
            return true;
        }catch(ForbiddenException e){
            // this indicates that the user does, in fact, exist, we just can't
            // access them.
            return true;
        }catch(NotFoundException e){
            return false;
        }
    }


    private void init(){
        users = target(getEndpoints().getDirectoryUri()).path("users");
        roles = target(getEndpoints().getAuthzUri())
                .path("roles")
                .path("user");
    }

    private WebTarget users(){
        return users;
    }

    private User create(Form params){
        return users().request(MediaType.APPLICATION_JSON).post(Entity.form(params), User.class);
    }
}
