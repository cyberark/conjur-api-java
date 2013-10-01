package net.conjur.api;

import net.conjur.api.authn.AuthnClient;
import net.conjur.api.authn.AuthnProvider;

import javax.ws.rs.client.WebTarget;

/**
 *
 */
public class Conjur extends Resource {
    private Users users;
    private Variables variables;

    public Conjur(AuthnProvider authn, Endpoints endpoints) {
        super(authn, endpoints);
    }

    public Conjur(AuthnProvider authn){
        this(authn, Endpoints.getDefault());
    }

    public Conjur(String username, String password, Endpoints endpoints){
        this(new AuthnClient(username, password, endpoints), endpoints);
    }

    public Conjur(String username, String password){
        this(new AuthnClient(username, password));
    }

    public Users users(){
        return users;
    }

    public Variables variables(){
        return variables;
    }

    private void init(){
        users = new Users(this);
        variables = new Variables(this);
    }

}
