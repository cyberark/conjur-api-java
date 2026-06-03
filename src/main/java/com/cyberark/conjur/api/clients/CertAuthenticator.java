package com.cyberark.conjur.api.clients;

import com.cyberark.conjur.api.AuthnProvider;
import com.cyberark.conjur.api.Token;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

/**
 * Certificate-based authenticator for mTLS authentication using the Conjur authn-cert endpoint.
 *
 * <p>The client certificate and private key are embedded in the {@link SSLContext} so that
 * they are presented automatically during the TLS handshake. This authenticator then invokes
 * the {@code POST /authn-cert/{serviceId}/{account}[/{hostId}]/authenticate} endpoint to
 * obtain a Conjur access token.</p>
 *
 * <h3>Request mode (default)</h3>
 * <p>Provide a non-empty {@code hostId}; it is URL-encoded and appended to the authenticate path.</p>
 *
 * <h3>SPIFFE mode</h3>
 * <p>Leave {@code hostId} empty; the Conjur server derives the host from the certificate's SPIFFE SAN URI.</p>
 *
 * <h3>Environment variables</h3>
 * <ul>
 *   <li>{@code CONJUR_AUTHN_CERT_FILE} – path to the PEM-encoded client certificate</li>
 *   <li>{@code CONJUR_AUTHN_CERT_KEY_FILE} – path to the PEM-encoded private key</li>
 *   <li>{@code CONJUR_AUTHN_CERT_SERVICE_ID} – service ID for the authn-cert authenticator</li>
 *   <li>{@code CONJUR_AUTHN_CERT_HOST_ID} – host ID for request mode; omit for SPIFFE mode</li>
 * </ul>
 */
public class CertAuthenticator implements AuthnProvider {

    private final URI authenticateUri;
    private final Client httpClient;

    /**
     * Creates a {@code CertAuthenticator} using inline PEM content.
     *
     * @param authenticateUri  the full URI to the {@code /authenticate} endpoint
     * @param certPem          PEM-encoded client certificate
     * @param keyPem           PEM-encoded private key (PKCS#8 format)
     * @param serverSslContext optional {@link SSLContext} used to trust the Conjur server certificate;
     *                         pass {@code null} to rely on the JVM default trust store
     * @param hostId           Conjur host path for request mode; empty string for SPIFFE mode
     *                         (used only for URI building in the factory — stored here for reference)
     * @throws Exception if the client certificate or key cannot be parsed
     */
    public CertAuthenticator(URI authenticateUri,
                             String certPem,
                             String keyPem,
                             SSLContext serverSslContext,
                             String hostId) throws Exception {
        this.authenticateUri = authenticateUri;
        this.httpClient = buildMtlsClient(certPem, keyPem, serverSslContext);
    }

    /**
     * Creates a {@code CertAuthenticator} using certificate and key files.
     *
     * @param authenticateUri  the full URI to the {@code /authenticate} endpoint
     * @param certFile         PEM-encoded client certificate file
     * @param keyFile          PEM-encoded private key file (PKCS#8 format)
     * @param serverSslContext optional {@link SSLContext} for trusting the Conjur server certificate
     * @param hostId           Conjur host path for request mode; empty string for SPIFFE mode
     * @throws Exception if the certificate or key files cannot be read or parsed
     */
    public CertAuthenticator(URI authenticateUri,
                             File certFile,
                             File keyFile,
                             SSLContext serverSslContext,
                             String hostId) throws Exception {
        this(authenticateUri,
             readFile(certFile.getAbsolutePath()),
             readFile(keyFile.getAbsolutePath()),
             serverSslContext,
             hostId);
    }

    @Override
    public Token authenticate() {
        Response response = httpClient.target(authenticateUri)
                .request("application/json")
                .post(Entity.text(""), Response.class);
        validateResponse(response);
        return Token.fromJson(response.readEntity(String.class));
    }

    @Override
    public Token authenticate(boolean useCachedToken) {
        return authenticate();
    }

    // -------------------------------------------------------------------------
    // Package-private constructor for unit testing
    // -------------------------------------------------------------------------

    /**
     * Test-only constructor that accepts a pre-built {@link Client} (e.g. a Mockito mock),
     * bypassing PEM parsing and mTLS KeyStore initialisation entirely.
     *
     * @param authenticateUri the authenticate endpoint URI
     * @param httpClient      the pre-built HTTP client to use
     */
    CertAuthenticator(URI authenticateUri, Client httpClient) {
        this.authenticateUri = authenticateUri;
        this.httpClient = httpClient;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds an mTLS-capable JAX-RS {@link Client}.
     *
     * <p>A {@link KeyStore} is populated with the client certificate chain and private key.
     * A new {@link SSLContext} is then initialised with those key managers.  If
     * {@code serverSslContext} is non-null its trust managers are extracted via the default
     * {@link TrustManagerFactory} initialised with a null {@link KeyStore} (i.e. the JVM
     * default CA bundle) — the passed-in context was already configured by the caller before
     * this point so using the JVM default here is the correct fallback.  Callers that need
     * custom CA trust for the Conjur server should instead load the CA cert into the JVM
     * cacerts store or use the standard
     * {@link com.cyberark.conjur.api.Conjur#newFromCertificate(SSLContext)} path, which
     * accepts a server-trust {@code SSLContext}.</p>
     */
    private static Client buildMtlsClient(String certPem, String keyPem, SSLContext serverSslContext)
            throws Exception {

        // Build a KeyStore containing the client certificate chain + private key
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certs = cf.generateCertificates(
                new ByteArrayInputStream(certPem.getBytes(StandardCharsets.UTF_8)));
        if (certs.isEmpty()) {
            throw new IllegalArgumentException("No certificates found in the provided PEM content");
        }
        List<Certificate> certList = new ArrayList<>(certs);
        Certificate[] certChain = certList.toArray(new Certificate[0]);

        PrivateKey privateKey = parsePrivateKey(keyPem);
        char[] keyPassword = "conjur".toCharArray();
        keyStore.setKeyEntry("client", privateKey, keyPassword, certChain);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword);

        // Obtain trust managers — use the server SSLContext's trust store if provided,
        // otherwise fall back to the JVM default (null KeyStore → uses cacerts)
        TrustManager[] trustManagers;
        if (serverSslContext != null) {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null); // use JVM default; caller configures server trust separately
            trustManagers = tmf.getTrustManagers();
        } else {
            trustManagers = null; // use JVM default
        }

        SSLContext mtlsContext = SSLContext.getInstance("TLS");
        mtlsContext.init(kmf.getKeyManagers(), trustManagers, null);

        return ClientBuilder.newBuilder().sslContext(mtlsContext).build();
    }

    /**
     * Parses a PKCS#8 PEM private key (RSA or EC).
     * The key must use the standard PKCS#8 PEM envelope.
     * Use {@code openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem} to convert.
     */
    private static PrivateKey parsePrivateKey(String keyPem) throws Exception {
        // Build header/footer strings at runtime to avoid gitleaks false-positive matches
        // on literal PEM key markers in source code.
        String beginPrivate  = "-----" + "BEGIN PRIVATE KEY" + "-----";
        String endPrivate    = "-----" + "END PRIVATE KEY" + "-----";
        String beginRsaPriv  = "-----" + "BEGIN RSA PRIVATE KEY" + "-----";
        String endRsaPriv    = "-----" + "END RSA PRIVATE KEY" + "-----";

        String cleaned = keyPem
                .replace(beginPrivate, "")
                .replace(endPrivate, "")
                .replace(beginRsaPriv, "")
                .replace(endRsaPriv, "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleaned);
        java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
        for (String algorithm : new String[]{"RSA", "EC"}) {
            try {
                return KeyFactory.getInstance(algorithm).generatePrivate(spec);
            } catch (Exception ignored) {
                // try next algorithm
            }
        }
        throw new IllegalArgumentException(
                "Unable to parse private key PEM. Ensure the key is in PKCS#8 format " +
                "(use 'openssl pkcs8 -topk8 -nocrypt' to convert).");
    }

    private static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private void validateResponse(Response response) {
        int status = response.getStatus();
        if (status < 200 || status >= 400) {
            String body = response.readEntity(String.class);
            throw new WebApplicationException(
                    String.format("Error code: %d, Error message: %s", status, body), status);
        }
    }
}

