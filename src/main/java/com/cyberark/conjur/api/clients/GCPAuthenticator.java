package com.cyberark.conjur.api.clients;

import static com.cyberark.conjur.util.EncodeUriComponent.encodeUriComponent;

import com.cyberark.conjur.api.AuthnProvider;
import com.cyberark.conjur.api.Token;
import com.cyberark.conjur.util.Args;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GCPAuthenticator implements AuthnProvider {

    public static final String GCP_METADATA_FLAVOR_HEADER_NAME = "Metadata-Flavor";
    public static final String GCP_METADATA_FLAVOR_HEADER_VALUE = "Google";
    public static final String DEFAULT_GCP_IDENTITY_URL =
            "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/identity";

    private final Client conjurClient;
    private final Client gcpClient;
    private final String conjurUri;
    private final String conjurAccount;
    private final String identity;
    private final String authenticator;
    private final String gcpIdentityUrl;

    private String jwt;

    public GCPAuthenticator(
            String conjurUri,
            String conjurAccount,
            String identity) {
        this(
                ClientBuilder.newClient(),
                ClientBuilder.newClient(),
                conjurUri,
                conjurAccount,
                identity,
                "authn-jwt/gcp",
                null,
                DEFAULT_GCP_IDENTITY_URL
        );
    }

    public GCPAuthenticator(
            Client conjurClient,
            Client gcpClient,
            String conjurUri,
            String conjurAccount,
            String identity,
            String authenticator,
            String jwt,
            String gcpIdentityUrl) {
        this.conjurClient = Args.notNull(conjurClient, "conjurClient");
        this.gcpClient = Args.notNull(gcpClient, "gcpClient");
        this.conjurUri = Args.notBlank(conjurUri, "conjurUri");
        this.conjurAccount = Args.notBlank(conjurAccount, "conjurAccount");
        this.identity = Args.notBlank(identity, "identity");
        this.authenticator = requireAuthenticatorWithServiceId(authenticator);
        this.jwt = jwt;
        this.gcpIdentityUrl = Args.notBlank(gcpIdentityUrl, "gcpIdentityUrl");
    }

    @Override
    public Token authenticate() {
        refreshJwt();
        return exchangeJwtForConjurToken(jwt);
    }

    @Override
    public Token authenticate(boolean useCachedToken) {
        return authenticate();
    }

    public void refreshJwt() {
        if (jwt == null || jwt.isEmpty()) {
            jwt = fetchGcpJwt();
        }
    }

    public String getJwt() {
        return jwt;
    }

    public String getConjurAuthenticateUrl() {
        String normalizedConjurUri = conjurUri.endsWith("/")
                ? conjurUri.substring(0, conjurUri.length() - 1)
                : conjurUri;

        return normalizedConjurUri + "/" + authenticator + "/" + conjurAccount + "/"
                + encodeUriComponent(identity) + "/authenticate";
    }

    String buildAudience() {
        String normalizedIdentity = identity.startsWith("host/")
                ? identity.substring("host/".length())
                : identity;

        return "conjur/" + conjurAccount + "/host/" + normalizedIdentity;
    }

    protected String fetchGcpJwt() {
        String metadataUrl = gcpIdentityUrl
                + "?audience=" + encodeQueryParam(buildAudience())
                + "&format=full";

        try (Response response = gcpClient
                .target(metadataUrl)
                .request("text/plain")
                .header(GCP_METADATA_FLAVOR_HEADER_NAME, GCP_METADATA_FLAVOR_HEADER_VALUE)
                .get(Response.class)) {

            validateResponse(response, "GCP metadata token request failed");
            String body = response.readEntity(String.class);

            if (body == null || body.trim().isEmpty()) {
                throw new IllegalStateException("GCP metadata service returned an empty token.");
            }

            return body;
        }
    }

    protected Token exchangeJwtForConjurToken(String gcpJwt) {
        Form form = new Form().param("jwt", gcpJwt);

        try (Response response = conjurClient
                .target(getConjurAuthenticateUrl())
                .request("text/plain")
                .header("Accept-Encoding", "base64")
                .post(Entity.form(form), Response.class)) {

            validateResponse(response, "Conjur GCP authenticate request failed");
            String encodedToken = response.readEntity(String.class);
            return Token.fromJson(base64DecodeToUtf8(encodedToken));
        }
    }

    private void validateResponse(Response response, String prefix) {
        int status = response.getStatus();
        if (status < 200 || status >= 400) {
            String body = response.readEntity(String.class);
            throw new WebApplicationException(
                    String.format("%s: status=%d body=%s", prefix, status, body),
                    status
            );
        }
    }

    private String base64DecodeToUtf8(String value) {
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Conjur response was not valid base64", e);
        }
    }

    private static String encodeQueryParam(String value) {
        return encodeUriComponent(value).replaceAll("\\+", "%20");
    }

    // Validates that authenticator includes a service-id segment (e.g. "authn-jwt/gcp").
    // Without the service-id Conjur returns 404 with no clear indication of the cause.
    private static String requireAuthenticatorWithServiceId(String authenticator) {
        Args.notBlank(authenticator, "authenticator");
        if (!authenticator.contains("/")) {
            throw new IllegalArgumentException(
                    "authenticator must include a service-id (e.g. \"authn-jwt/gcp\"), got: " + authenticator);
        }
        return authenticator;
    }
}