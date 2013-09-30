package net.conjur.api;

import net.conjur.util.Args;

import java.net.URI;
import java.util.Properties;

/**
 * <code>BasicEndpoints</code> provides a simple representation of conjur service endpoints.
 */
public class BasicEndpoints extends Endpoints {
    public static final String AUTHN_URI_PROPERTY = "net.conjur.api.basicEndpoints.authnUri";
    public static final String AUTHZ_URI_PROPERTY = "net.conjur.api.basicEndpoints.authzUri";
    public static final String DIRECTORY_URI_PROPERTY = "net.conjur.api.basicEndpoints.directoryUri";


    private URI authnUri;
    private URI authzUri;
    private URI directoryUri;

    public BasicEndpoints(){}

    public BasicEndpoints(final URI authnUri,
                          final URI authzUri,
                          final URI directoryUri) {
        this.authnUri = authnUri;
        this.authzUri = authzUri;
        this.directoryUri = directoryUri;
    }

    public BasicEndpoints(final String authnUri,
                          final String authzUri,
                          final String directoryUri){
        setAuthnUri(authnUri);
        setDirectoryUri(directoryUri);
        setAuthzUri(authzUri);
    }

    /**
     * Creates a <code>BasicEndpoints</code> instance using {@link System#getProperties()}.
     * @see #fromProperties(java.util.Properties)
     */
    public static BasicEndpoints fromSystemProperties(){
        return fromProperties(System.getProperties());
    }

    /**
     * Creates a <code>BasicEndpoints</code> configured from the given properties.
     *
     * @see BasicEndpoints#AUTHN_URI_PROPERTY
     * @see BasicEndpoints#AUTHZ_URI_PROPERTY
     * @see BasicEndpoints#DIRECTORY_URI_PROPERTY
     *
     * @param properties A properties object containing {@link BasicEndpoints#AUTHN_URI_PROPERTY},
     *                   {@link BasicEndpoints#AUTHZ_URI_PROPERTY}, and {@link BasicEndpoints#DIRECTORY_URI_PROPERTY}.
     */
    public static BasicEndpoints fromProperties(final Properties properties){
        Args.notNull(properties);
        return new BasicEndpoints(properties.getProperty(AUTHN_URI_PROPERTY),
                properties.getProperty(AUTHZ_URI_PROPERTY),
                properties.getProperty(DIRECTORY_URI_PROPERTY));
    }

    @Override
    public URI getAuthnUri() {
        return authnUri;
    }

    @Override
    public URI getDirectoryUri() {
        return directoryUri;
    }

    @Override
    public URI getAuthzUri() {
        return authzUri;
    }

    public void setAuthnUri(final URI authnUri) {
        this.authnUri = authnUri;
    }

    public void setAuthzUri(final URI authzUri) {
        this.authzUri = authzUri;
    }

    public void setDirectoryUri(final URI directoryUri) {
        this.directoryUri = directoryUri;
    }


    public void setAuthnUri(final String authnUri) {
        this.authnUri = authnUri == null ? null : URI.create(authnUri);
    }

    public void setAuthzUri(final String authzUri) {
        this.authzUri = authzUri == null ? null : URI.create(authzUri);
    }

    public void setDirectoryUri(final String directoryUri) {
        this.directoryUri = directoryUri == null ? null : URI.create(directoryUri);
    }
}
