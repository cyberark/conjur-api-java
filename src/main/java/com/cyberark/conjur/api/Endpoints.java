package com.cyberark.conjur.api;

import com.cyberark.conjur.util.Args;
import com.cyberark.conjur.util.Properties;

import java.io.Serializable;
import java.net.URI;

/**
 * An <code>Endpoints</code> instance provides endpoint URIs for the various Conjur services.
 *
 * <p>The canonical way to construct an {@code Endpoints} is from an appliance URL and account name.
 * All service URIs are derived from these two values:</p>
 * <ul>
 *   <li>Authentication: {@code {applianceUrl}/authn/{account}}</li>
 *   <li>Secrets (single): {@code {applianceUrl}/secrets/{account}/variable}</li>
 *   <li>Secrets (batch): {@code {applianceUrl}/secrets}</li>
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
     * Create Endpoints from appliance URL and account, using standard authentication.
     *
     * @param applianceUrl the base Conjur appliance URL (e.g. {@code https://conjur.example.com})
     * @param account      the Conjur account name (e.g. {@code conjur} for SaaS, or your org name)
     */
    public Endpoints(String applianceUrl, String account) {
        this(applianceUrl, account, applianceUrl + "/authn");
    }

    /**
     * Create Endpoints from appliance URL, account, and a custom authentication URL.
     * Use this when authenticating via a non-standard authenticator (LDAP, OIDC, etc.).
     *
     * @param applianceUrl the base Conjur appliance URL
     * @param account      the Conjur account name
     * @param authnUrl     the authentication service base URL
     *                     (e.g. {@code https://conjur.example.com/authn-ldap/my-service})
     */
    public Endpoints(String applianceUrl, String account, String authnUrl) {
        this.applianceUrl = Args.notNull(applianceUrl, "applianceUrl");
        this.account = Args.notNull(account, "account");
        this.authnUri = URI.create(String.format("%s/%s", authnUrl, account));
        this.secretsUri = URI.create(String.format("%s/secrets/%s/variable", applianceUrl, account));
    }

    public URI getAuthnUri() { return authnUri; }

    public URI getSecretsUri() { return secretsUri; }

    public String getAccount() { return account; }

    public String getApplianceUrl() { return applianceUrl; }

    /**
     * Returns the base URI for batch secret retrieval: {@code {applianceUrl}/secrets}
     *
     * @return the batch secrets URI
     */
    public URI getBatchSecretsUri() {
        return URI.create(applianceUrl + "/secrets");
    }

    /**
     * Create Endpoints from system properties / environment variables.
     * Reads {@code CONJUR_ACCOUNT}, {@code CONJUR_APPLIANCE_URL}, and optionally {@code CONJUR_AUTHN_URL}.
     */
    public static Endpoints fromSystemProperties() {
        String account = Properties.getMandatoryProperty(Constants.CONJUR_ACCOUNT_PROPERTY);
        String applianceUrl = Properties.getMandatoryProperty(Constants.CONJUR_APPLIANCE_URL_PROPERTY);
        String authnUrl = Properties.getMandatoryProperty(
                Constants.CONJUR_AUTHN_URL_PROPERTY, applianceUrl + "/authn");

        return new Endpoints(applianceUrl, account, authnUrl);
    }

    /**
     * Create Endpoints using the authentication URL from the given credentials.
     * Account and appliance URL are read from system properties / environment variables.
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
