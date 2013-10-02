package net.conjur.api;


import net.conjur.util.Lang;

import java.io.Serializable;
import java.net.URI;

import static net.conjur.util.Lang.getOrElse;

/**
 * An <code>Endpoints</code> instance provides endpoint URIs for the various conjur services.
 * This is an abstract base class allowing users to provide their own endpoints, for example when
 * the conjur services are being hosted on premises.
 *
 * @see HostedEndpoints
 * @see BasicEndpoints
 */
public abstract class Endpoints implements Serializable {
    /**
     * Default endpoints implementation for convenience.  Uses
     * {@link net.conjur.api.HostedEndpoints#fromSystemProperties()}.
     */
    public static final Endpoints DEFAULT_ENDPOINTS = HostedEndpoints.fromSystemProperties();

    // java constructors are silly
    Endpoints(){}

    /**
     * Return the conjur account for this endpoints
     */
    public abstract String getAccount();

    /**
     * Get the base URI for the conjur authentication (authn) service.
     */
    public abstract URI getAuthnUri();

    /**
     *  <p>Get the base URI for the conjur directory service.</p>
     *  <p><b>Note:</b> For historical reasons, the directory service is sometimes called the <em>core</em>
     *  service.</p>
     */
    public abstract URI getDirectoryUri();


    /**
     * The base URI for the conjur authorization (authz) service.
     */
    public abstract URI getAuthzUri();

    private static Endpoints defaultEndpoints;

    /**
     * Get global default endpoints.
     *
     * <p>Returns either the instance passed to {@link #setDefault(Endpoints)} {@link #DEFAULT_ENDPOINTS}
     *
     * @see HostedEndpoints#fromSystemProperties()
     */
    public static Endpoints getDefault(){
        return defaultEndpoints = getOrElse(defaultEndpoints, DEFAULT_ENDPOINTS);
    }


    /**
     * <p>Set global default endpoints, which will be returned by {@link #getDefault()}.</p>
     *
     * <p>Passing null to this method will cause {@link #getDefault()}to return
     * {@link #DEFAULT_ENDPOINTS}</p>
     */
    public static void setDefault(Endpoints defaultEndpoints){
        Endpoints.defaultEndpoints = defaultEndpoints;
    }

}
