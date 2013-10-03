package net.conjur.api;

import net.conjur.api.authn.AuthnClient;
import net.conjur.api.authn.AuthnProvider;

/**
 *
 */
public class Conjur extends Resource {
    private Users users;
    private Variables variables;

    public Conjur(AuthnProvider authn, Endpoints endpoints) {
        super(authn, endpoints);
        init();
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

    public Conjur(Credentials credentials){
        this(credentials.getUsername(), credentials.getPassword());
    }

    public Conjur(Credentials credentials, Endpoints endpoints){
        this(credentials.getUsername(), credentials.getPassword(), endpoints);
    }

    public Users users(){
        return users;
    }

    public Variables variables(){
        return variables;
    }

    public AuthnProvider getAuthn(){
        return super.getAuthn();
    }

    private void init(){
        users = new Users(this);
        variables = new Variables(this);
    }

}
