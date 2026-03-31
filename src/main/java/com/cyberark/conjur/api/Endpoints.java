package com.cyberark.conjur.api;

import com.cyberark.conjur.util.Args;
import com.cyberark.conjur.util.Properties;

import java.io.Serializable;
import java.net.URI;

/**
 * An <code>Endpoints</code> instance provides endpoint URIs for the various Conjur services.
 *
 * <p>Preferred construction is via {@link #fromSystemProperties()} or
 * {@link #fromCredentials(Credentials)}. These paths derive service URIs from an
 * appliance URL and account name:</p>
 * <ul>
 *   <li>Authentication: {@code {applianceUrl}/authn/{account}}</li>
 *   <li>Secrets (single): {@code {applianceUrl}/secrets/{account}/variable}</li>
 *   <li>Secrets (batch): {@code {applianceUrl}/secrets}</li>
 *   <li>Resources: {@code {applianceUrl}/resources/{account}}</li>
 * </ul>
 *
 * <p>For non-standard authenticators (LDAP, OIDC, etc.), supply a custom authn URL.</p>
 */
public class Endpoints implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String applianceUrl;
    private final String account;
    private final URI authnUri;
    private final URI secretsUri;

    /**
     * Constructs endpoints from fully-qualified authn and secrets URIs.
     *
     * <p>This legacy constructor preserves only the explicitly supplied authn and
     * secrets URIs. It does not initialize derived state needed by newer APIs,
     * such as batch secret retrieval. Use the factory methods for fully initialized
     * endpoint instances.</p>
     *
     * @param authnUri   the authn service URI
     * @param secretsUri the secrets service URI
     * @deprecated Use factory methods for new code:
     * {@link #fromSystemProperties()} or {@link #fromCredentials(Credentials)}.
     */
    @Deprecated
    public Endpoints(final URI authnUri, final URI secretsUri) {
        URI safeAuthnUri = Args.notNull(authnUri, "authnUri");
        URI safeSecretsUri = Args.notNull(secretsUri, "secretsUri");

        this.account = null;
        this.applianceUrl = null;
        this.authnUri = safeAuthnUri;
        this.secretsUri = safeSecretsUri;
    }

    /**
     * Constructs endpoints from fully-qualified authn and secrets URI strings.
     *
     * <p>This legacy constructor preserves only the explicitly supplied authn and
     * secrets URIs. It does not initialize derived state needed by newer APIs,
     * such as batch secret retrieval. Use the factory methods for fully initialized
     * endpoint instances.</p>
     *
     * @param authnUri   the authn service URI string
     * @param secretsUri the secrets service URI string
     * @deprecated Use factory methods for new code:
     * {@link #fromSystemProperties()} or {@link #fromCredentials(Credentials)}.
     */
    @Deprecated
    public Endpoints(String authnUri, String secretsUri) {
        this(URI.create(authnUri), URI.create(secretsUri));
    }

    /**
     * Canonical constructor; called only by the factory methods in this class.
     *
     */
    private Endpoints(String applianceUrl, String account, String authnUrl) {
        this.applianceUrl = Args.notNull(applianceUrl, "applianceUrl");
        this.account = Args.notNull(account, "account");
        this.authnUri = URI.create(String.format("%s/%s", Args.notNull(authnUrl, "authnUrl"), this.account));
        this.secretsUri = URI.create(String.format("%s/secrets/%s/variable", this.applianceUrl, this.account));
    }

    public URI getAuthnUri() {
        return authnUri;
    }

    public URI getSecretsUri() {
        return secretsUri;
    }

    /**
     * Returns the Conjur account name associated with these endpoints.
     *
     * @return the configured Conjur account name
     * @throws IllegalStateException when account is unavailable on this instance
     */
    public String getAccount() {
        if (account == null) {
            throw new IllegalStateException(
                    "Account is unavailable for Endpoints instances created with deprecated constructors"
            );
        }
        return account;
    }


    /**
     * Returns the base URI for batch secret retrieval: {@code {applianceUrl}/secrets}
     *
     * @return the batch secrets URI
     * @throws IllegalStateException when appliance URL is unavailable on this instance
     */
    public URI getBatchSecretsUri() {
        if (applianceUrl == null) {
            throw new IllegalStateException(
                    "Batch secrets URI is unavailable for Endpoints instances created with deprecated constructors"
            );
        }
        return URI.create(applianceUrl + "/secrets");
    }

    /**
     * Returns the URI for listing resources: {@code {applianceUrl}/resources/{account}}
     *
     * @return the resources URI
     */
    public URI getResourcesUri() {
        return URI.create(String.format("%s/resources/%s", applianceUrl, account));
    }

    /**
     * Creates endpoints from system properties / environment variables.
     *
     * <p>Reads the following configuration values:</p>
     * <ul>
     *   <li>{@code CONJUR_ACCOUNT} (required)</li>
     *   <li>{@code CONJUR_APPLIANCE_URL} (required)</li>
     *   <li>{@code CONJUR_AUTHN_URL} (optional, defaults to {@code {applianceUrl}/authn})</li>
     * </ul>
     *
     * @return an {@code Endpoints} instance derived from configured Conjur settings
     * @throws IllegalArgumentException if required configuration is missing
     */
    public static Endpoints fromSystemProperties() {
        String account = Properties.getMandatoryProperty(Constants.CONJUR_ACCOUNT_PROPERTY);
        String applianceUrl = Properties.getMandatoryProperty(Constants.CONJUR_APPLIANCE_URL_PROPERTY);
        String authnUrl = Properties.getMandatoryProperty(Constants.CONJUR_AUTHN_URL_PROPERTY, applianceUrl + "/authn");

        return new Endpoints(applianceUrl, account, authnUrl);
    }

    /**
     * Creates endpoints using the account/appliance values from configuration and
     * the authenticator URL from the provided credentials.
     *
     * <p>Reads the following configuration values:</p>
     * <ul>
     *   <li>{@code CONJUR_ACCOUNT} (required)</li>
     *   <li>{@code CONJUR_APPLIANCE_URL} (required)</li>
     * </ul>
     *
     * @param credentials credentials whose {@code authnUrl} is used to build the authn endpoint
     * @return an {@code Endpoints} instance combining configured account/appliance with the provided authn URL
     * @throws IllegalArgumentException if required configuration is missing
     */
    public static Endpoints fromCredentials(Credentials credentials) {
        String account = Properties.getMandatoryProperty(Constants.CONJUR_ACCOUNT_PROPERTY);
        String applianceUrl = Properties.getMandatoryProperty(Constants.CONJUR_APPLIANCE_URL_PROPERTY);

        return new Endpoints(applianceUrl, account, credentials.getAuthnUrl());
    }

    @Override
    public String toString() {
        return "Endpoints{" +
                "applianceUrl=" + applianceUrl +
                ", account=" + account +
                ", authnUri=" + authnUri +
                ", secretsUri=" + secretsUri +
                '}';
    }
}
