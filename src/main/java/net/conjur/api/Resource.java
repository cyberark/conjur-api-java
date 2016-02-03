package net.conjur.api;

import static net.conjur.util.EncodeUriComponent.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Represents a Conjur resource.
 */
public class Resource extends RestResource implements HasResource {
    private final UriBuilder resourceUriBuilder =
            UriBuilder.fromUri(getEndpoints().getAuthzUri())
            .segment("{account}", "resources", "{kind}", "{id}");

    private final ConjurIdentifier resourceId;

    Resource(RestResource relative, ConjurIdentifier resourceId){
        super(relative);
        this.resourceId = resourceId;
    }

    public Resource getResource(){
        return this;
    }

    public ConjurIdentifier getResourceId(){
        return resourceId;
    }

    public boolean exists(){
        return checkExists(getResourceUriBuilder().build());
    }

    public void createIfNotFound(HasRole actingAs){
        if(!exists()){
            create(actingAs);
        }
    }

    public void createIfNotFound(){
        createIfNotFound(null);
    }

    public void create(){
        create(null);
    }

    public void create(HasRole actingAs){

        if(actingAs == null){
            actingAs = getCurrentRole();
        }

        URI uri = getResourceUriBuilder()
                .queryParam("acting_as", encodeUriComponent(actingAs.getRole().getRoleId().toString()))
                .build();

        target(uri).request().put(Entity.entity("","text/plain"));
    }



    public void permit(HasRole role, String privilege){
        ConjurIdentifier roleId = role.getRole().getRoleId();
        URI uri = getResourceUriBuilder()
                .queryParam("permit", "true")
                .queryParam("privilege", encodeUriComponent(privilege))
                .queryParam("role", encodeUriComponent(roleId.toString())).build();

        target(uri).request().post(Entity.entity("", "text/plain"), String.class);
    }

    public void deny(HasRole role, String privilege){
        String roleId = role.getRole().getRoleId().toString();
        final URI uri = getResourceUriBuilder()
                .queryParam("deny", "true")
                .queryParam("privilege", encodeUriComponent(privilege))
                .queryParam("role", encodeUriComponent(roleId))
                .build();
        System.out.println("Revoking with " + uri.toString());
        String result = target(uri).request().post(Entity.entity("", "text/plain"), String.class);
        System.err.println("revoke result: " + result);
    }

    private UriBuilder getResourceUriBuilder(){
        return resourceUriBuilder.clone().resolveTemplate("account", getAccount())
                .resolveTemplate("kind", getResourceId().getKind())
                .resolveTemplate("id", getResourceId().getId());
    }
}
