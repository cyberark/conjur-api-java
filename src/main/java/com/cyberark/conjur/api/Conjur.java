package com.cyberark.conjur.api;

import com.cyberark.conjur.api.clients.CertAuthenticator;
import com.cyberark.conjur.api.clients.ResourceClient;
import com.cyberark.conjur.util.Properties;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.URI;

/**
 * Entry point for the Conjur API client.
 */
public class Conjur {

    private final Variables variables;
    private final Resources resources;

    /**
     * Create a Conjur instance that uses credentials from the system properties
     */
    public Conjur(){
        this(Credentials.fromSystemProperties());
    }

    /**
     * Create a Conjur instance that uses credentials from the system properties
     * @param sslContext the {@link SSLContext} to use for connections to Conjur server
     */
    public Conjur(SSLContext sslContext){
        this(Credentials.fromSystemProperties(), sslContext);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     */
    public Conjur(String username, String password) {
        this(new Credentials(username, password));
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     * @param sslContext the {@link SSLContext} to use for connections to Conjur server
     */
    public Conjur(String username, String password, SSLContext sslContext) {
        this(new Credentials(username, password), sslContext);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     * @param authnUrl the conjur authentication url
     */
    public Conjur(String username, String password, String authnUrl) {
        this(new Credentials(username, password, authnUrl));
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param username username for the Conjur identity to authenticate as
     * @param password password or api key for the Conjur identity to authenticate as
     * @param authnUrl the conjur authentication url
     * @param sslContext the {@link SSLContext} to use for connections to Conjur server
     */
    public Conjur(String username, String password, String authnUrl, SSLContext sslContext) {
        this(new Credentials(username, password, authnUrl), sslContext);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param credentials the conjur identity to authenticate as
     */
    public Conjur(Credentials credentials) {
        this(credentials, null);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param credentials the conjur identity to authenticate as
     * @param sslContext the {@link SSLContext} to use for connections to Conjur server
     */
    public Conjur(Credentials credentials, SSLContext sslContext) {
        this(new ResourceClient(credentials, Endpoints.fromCredentials(credentials), sslContext));
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param token the conjur authorization token to use
     */
    public Conjur(Token token) {
        this(token, null);
    }

    /**
     * Create a Conjur instance that uses a ResourceClient &amp; an AuthnClient constructed with the given credentials
     * @param token the conjur authorization token to use
     * @param sslContext the {@link SSLContext} to use for connections to Conjur server
     */
    public Conjur(Token token, SSLContext sslContext) {
        this(new ResourceClient(token, Endpoints.fromSystemProperties(), sslContext));
    }

    Conjur(ResourceClient resourceClient) {
        variables = new Variables(resourceClient);
        resources = new Resources(resourceClient);
    }

    // -------------------------------------------------------------------------
    // Certificate authenticator factory methods
    // -------------------------------------------------------------------------

    /**
     * Creates a {@code Conjur} instance that authenticates using the authn-cert (mTLS) endpoint.
     *
     * <p>Reads configuration from environment variables / system properties:</p>
     * <ul>
     *   <li>{@code CONJUR_ACCOUNT} (required)</li>
     *   <li>{@code CONJUR_APPLIANCE_URL} (required)</li>
     *   <li>{@code CONJUR_AUTHN_CERT_SERVICE_ID} (required)</li>
     *   <li>{@code CONJUR_AUTHN_CERT_FILE} (required) – path to PEM client certificate</li>
     *   <li>{@code CONJUR_AUTHN_CERT_KEY_FILE} (required) – path to PEM private key</li>
     *   <li>{@code CONJUR_AUTHN_CERT_HOST_ID} (optional) – Conjur host ID; omit for SPIFFE mode</li>
     * </ul>
     *
     * @return a fully configured {@code Conjur} instance using certificate authentication
     * @throws Exception if the certificate or key cannot be loaded or the authenticator cannot be built
     */
    public static Conjur newFromCertificate() throws Exception {
        return newFromCertificate(null);
    }

    /**
     * Creates a {@code Conjur} instance that authenticates using the authn-cert (mTLS) endpoint.
     *
     * <p>See {@link #newFromCertificate()} for the full list of environment variables.</p>
     *
     * @param serverSslContext optional {@link SSLContext} used to trust the Conjur server certificate;
     *                         pass {@code null} to rely on the JVM default trust store
     * @return a fully configured {@code Conjur} instance using certificate authentication
     * @throws Exception if the certificate or key cannot be loaded or the authenticator cannot be built
     */
    public static Conjur newFromCertificate(SSLContext serverSslContext) throws Exception {
        String certFile = Properties.getMandatoryProperty(Constants.CONJUR_AUTHN_CERT_FILE_PROPERTY);
        String keyFile  = Properties.getMandatoryProperty(Constants.CONJUR_AUTHN_CERT_KEY_FILE_PROPERTY);
        String serviceId = Properties.getMandatoryProperty(Constants.CONJUR_AUTHN_CERT_SERVICE_ID_PROPERTY);
        String hostId  = Properties.getMandatoryProperty(Constants.CONJUR_AUTHN_CERT_HOST_ID_PROPERTY, "");

        Endpoints endpoints = Endpoints.fromSystemProperties();
        URI certAuthnUri = buildCertAuthenticateUri(endpoints, serviceId, hostId);

        CertAuthenticator authn = new CertAuthenticator(
                certAuthnUri,
                new File(certFile),
                new File(keyFile),
                serverSslContext,
                hostId);

        ResourceClient resourceClient = new ResourceClient(authn, endpoints, serverSslContext);
        return new Conjur(resourceClient);
    }

    /**
     * Creates a {@code Conjur} instance that authenticates using the authn-cert (mTLS) endpoint
     * from explicitly supplied PEM content.
     *
     * @param serviceId       the authn-cert service ID (e.g. {@code "acme-vm"})
     * @param hostId          Conjur host path for request mode; empty string for SPIFFE mode
     * @param certPem         PEM-encoded client certificate
     * @param keyPem          PEM-encoded private key (PKCS#8)
     * @param serverSslContext optional {@link SSLContext} for trusting the Conjur server certificate
     * @return a fully configured {@code Conjur} instance using certificate authentication
     * @throws Exception if the certificate or key cannot be parsed or the authenticator cannot be built
     */
    public static Conjur newFromCertificate(String serviceId,
                                            String hostId,
                                            String certPem,
                                            String keyPem,
                                            SSLContext serverSslContext) throws Exception {
        Endpoints endpoints = Endpoints.fromSystemProperties();
        URI certAuthnUri = buildCertAuthenticateUri(endpoints, serviceId, hostId);

        CertAuthenticator authn = new CertAuthenticator(certAuthnUri, certPem, keyPem, serverSslContext, hostId);
        ResourceClient resourceClient = new ResourceClient(authn, endpoints, serverSslContext);
        return new Conjur(resourceClient);
    }

    /**
     * Creates a {@code Conjur} instance that authenticates using the authn-cert (mTLS) endpoint
     * from certificate and key files.
     *
     * @param serviceId       the authn-cert service ID (e.g. {@code "acme-vm"})
     * @param hostId          Conjur host path for request mode; empty string for SPIFFE mode
     * @param certFile        PEM-encoded client certificate file
     * @param keyFile         PEM-encoded private key file (PKCS#8)
     * @param serverSslContext optional {@link SSLContext} for trusting the Conjur server certificate
     * @return a fully configured {@code Conjur} instance using certificate authentication
     * @throws Exception if the certificate or key files cannot be read or the authenticator cannot be built
     */
    public static Conjur newFromCertificate(String serviceId,
                                            String hostId,
                                            File certFile,
                                            File keyFile,
                                            SSLContext serverSslContext) throws Exception {
        Endpoints endpoints = Endpoints.fromSystemProperties();
        URI certAuthnUri = buildCertAuthenticateUri(endpoints, serviceId, hostId);

        CertAuthenticator authn = new CertAuthenticator(certAuthnUri, certFile, keyFile, serverSslContext, hostId);
        ResourceClient resourceClient = new ResourceClient(authn, endpoints, serverSslContext);
        return new Conjur(resourceClient);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds the full authenticate URI for the authn-cert endpoint.
     *
     * <ul>
     *   <li>Request mode: {@code {applianceUrl}/authn-cert/{serviceId}/{account}/{encodedHostId}/authenticate}</li>
     *   <li>SPIFFE mode:  {@code {applianceUrl}/authn-cert/{serviceId}/{account}/authenticate}</li>
     * </ul>
     */
    static URI buildCertAuthenticateUri(Endpoints endpoints, String serviceId, String hostId) {
        URI base = endpoints.getCertAuthnBaseUri(serviceId);
        if (hostId != null && !hostId.isEmpty()) {
            String encodedHostId = java.net.URLEncoder.encode(hostId, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return URI.create(base.toString() + "/" + encodedHostId + "/authenticate");
        }
        return URI.create(base.toString() + "/authenticate");
    }

    /**
     * Get a Variables instance configured with the same parameters as this instance.
     * @return the variables instance
     */
    public Variables variables() {
        return variables;
    }

    /**
     * Get a Resources instance configured with the same parameters as this instance.
     * @return the resources instance
     */
    public Resources resources() {
        return resources;
    }
}
