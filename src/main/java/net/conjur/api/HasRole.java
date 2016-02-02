package net.conjur.api;

/**
 * Represents things that have an associated Conjur role, such as a User or Host.
 * The Role class also implements this method for convenience by returning itself.
 */
public interface HasRole {
    /**
     * Gets the role associated with this object.
     * @return the role associated with this object.
     */
    Role getRole();
}
