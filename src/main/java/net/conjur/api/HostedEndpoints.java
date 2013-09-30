package net.conjur.api;

import net.conjur.util.Lang;

import java.net.URI;
import java.util.Properties;

import static net.conjur.util.Lang.getOrElse;

/**
 * <p>Represents the endpoints for hosted conjur services.</p>
 *
 * <p>The conjur service architecture consists of both <em>single-tenant</em> and
 * <em>multi-tenant</em> services.  The <em>single-tenant</em> services use a host for each
 * conjur <em>account</em>, while the <em>multi-tenant</em> services use a single host for all accounts
 * using a particular conjur <em>stack</em>.</p>
 *
 * <p>Multi-tenant services are organized into <em>stacks</em>.  Typically there is one stack for each
 * conjur version, currently named like <code>"v3"</code>.  There may also be special stacks to support
 * individual enterprise customer requirements. </p>
 *
 * <p>A <code>HostedEndpoints</code> instance is configured with an <code>account</code> and <code>stack</code>.
 * These are provided by Conjur when you sign up for an account.  A <em>sandbox</em> account is also available for
 * testing and development: this is the account used if no account parameter is provided.  You will normally not
 * need to set the <code>stack</code> parameter when creating an instance of this class.
 * </p>
 *
 * <p><code>HostedParameters</code> can be configured from {@link System#getProperties()} or a user supplied
 * {@link Properties} instance, using the keys {@link HostedEndpoints#ACCOUNT_PROPERTY_NAME} and
 * {@link HostedEndpoints#STACK_PROPERTY_NAME}.  If either property is not set, the defaults (
 * {@link HostedEndpoints#DEFAULT_ACCOUNT} and {@link HostedEndpoints#DEFAULT_STACK} are used.</p>
 *
 * <p>To support thread safety and programmer sanity this class is immutable.  If you really need mutability
 * consider using {@link BasicEndpoints} or rolling your own {@link Endpoints} implementation</p>
 */
public class HostedEndpoints extends Endpoints {
    public static final String ACCOUNT_PROPERTY_NAME = "net.conjur.api.account";
    public static final String DEFAULT_ACCOUNT = "sandbox";
    public static final String STACK_PROPERTY_NAME = "net.conjur.api.stack";
    public static final String DEFAULT_STACK   = "v4";

    // store properties on a BasicEndpoints instance.
    private final BasicEndpoints storage;

    /**
     * Create an instance configured using the given account and stack.
     * @param account the conjur account
     * @param stack the conjur stack
     */
    public HostedEndpoints(String account, String stack){
        account = getOrElse(account, DEFAULT_ACCOUNT);
        stack = getOrElse(stack, DEFAULT_STACK);
        this.storage = new BasicEndpoints(
           getServiceUri("authn", account),
           getServiceUri("authz", stack),
           getServiceUri("directory", account)
        );
    }

    /**
     * Create an instance using the given account and {@link HostedEndpoints#DEFAULT_STACK}.
     * @param account
     */
    public HostedEndpoints(String account){
        this(account, null);
    }

    /**
     * Create an instance using the given properties, with account and stack
     * taken from the properties {@link HostedEndpoints#ACCOUNT_PROPERTY_NAME}
     * and {@link HostedEndpoints#STACK_PROPERTY_NAME} respectively.
     */
    public HostedEndpoints(Properties config){
        this(config.getProperty(ACCOUNT_PROPERTY_NAME),
             config.getProperty(STACK_PROPERTY_NAME));
    }

    /**
     * Create an instance using {@link HostedEndpoints#DEFAULT_ACCOUNT} and
     * {@link HostedEndpoints#DEFAULT_STACK}.
     */
    public HostedEndpoints(){
        this(null, null);
    }

    /**
     * Create an instance configured from {@link System.getProperties()}
     * @see HostedEndpoints#HostedEndpoints(java.util.Properties)
     */
    public static Endpoints fromSystemProperties(){
        return new HostedEndpoints(System.getProperties());
    }

    @Override
    public URI getAuthnUri() {
        return storage.getAuthnUri();
    }

    @Override
    public URI getDirectoryUri() {
        return storage.getDirectoryUri();
    }

    @Override
    public URI getAuthzUri() {
        return storage.getAuthzUri();  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static URI getServiceUri(String service, String designator){
        return URI.create(String.format("https://%s-%s-conjur.herokuapp.com", service, designator));
    }
}
