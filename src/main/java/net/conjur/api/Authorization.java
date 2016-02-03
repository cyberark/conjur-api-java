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

    public Role getCurrentRole() {
        String username = getAuthn().getUsername();
        String kind = "user";
        if(username.startsWith("host/")){
            username = username.substring(5);
            kind = "host";
        }

        return getRole(kind + ":" + username);
    }
}
