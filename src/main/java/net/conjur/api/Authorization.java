package net.conjur.api;

public class Authorization extends RestResource {
    Authorization(RestResource relative){
        super(relative);
    }

    public Role getRole(String id){
        return new Role(this, resolveId(id));
    }

    public Resource getResource(String id){
        return new Resource(this, resolveId(id));
    }


}
