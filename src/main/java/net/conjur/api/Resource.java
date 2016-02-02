package net.conjur.api;

/**
 * Represents a Conjur resource.
 */
public class Resource extends RestResource implements HasResource {
    private final ConjurIdentifier resourceId;

    Resource(RestResource relative, ConjurIdentifier resourceId){
        super(relative);
        this.resourceId = resourceId;
    }

    public Resource getResource(){
        return this;
    }
}
