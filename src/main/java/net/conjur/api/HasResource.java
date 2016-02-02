package net.conjur.api;

/**
 * Represents things that are associated with a conjur resource.
 */
public interface HasResource {
    Resource getResource();
}
