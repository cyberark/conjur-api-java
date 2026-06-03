package com.cyberark.conjur.api;

public class Constants
{
    public static final String CONJUR_ACCOUNT_PROPERTY = "CONJUR_ACCOUNT";
    public static final String CONJUR_AUTHN_LOGIN_PROPERTY = "CONJUR_AUTHN_LOGIN";
    public static final String CONJUR_AUTHN_API_KEY_PROPERTY = "CONJUR_AUTHN_API_KEY";
    public static final String CONJUR_AUTHN_URL_PROPERTY = "CONJUR_AUTHN_URL";
    public static final String CONJUR_APPLIANCE_URL_PROPERTY = "CONJUR_APPLIANCE_URL";

    // Certificate authenticator (authn-cert / mTLS)
    /** Path to the PEM-encoded client certificate file. */
    public static final String CONJUR_AUTHN_CERT_FILE_PROPERTY = "CONJUR_AUTHN_CERT_FILE";
    /** Path to the PEM-encoded private key file for the client certificate. */
    public static final String CONJUR_AUTHN_CERT_KEY_FILE_PROPERTY = "CONJUR_AUTHN_CERT_KEY_FILE";
    /**
     * Service ID for the authn-cert authenticator (e.g. "acme-vm").
     * When set, {@code CONJUR_AUTHN_URL} is constructed as
     * {@code {applianceUrl}/authn-cert/{serviceId}}.
     */
    public static final String CONJUR_AUTHN_CERT_SERVICE_ID_PROPERTY = "CONJUR_AUTHN_CERT_SERVICE_ID";
    /**
     * Conjur host ID used in request mode (e.g. "host/vm-workloads/vm-01").
     * Leave empty for SPIFFE mode where the host is derived from the cert's SAN URI.
     */
    public static final String CONJUR_AUTHN_CERT_HOST_ID_PROPERTY = "CONJUR_AUTHN_CERT_HOST_ID";
}
