package com.cyberark.conjur.api.clients;

import static com.cyberark.conjur.util.EncodeUriComponent.encodeUriComponent;

import com.cyberark.conjur.api.AuthnProvider;
import com.cyberark.conjur.api.Token;
import com.cyberark.conjur.util.Args;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AzureAuthenticator implements AuthnProvider, AutoCloseable {

    private static final String IMDS_TOKEN_PATH = "/metadata/identity/oauth2/token";
    private static final String DEFAULT_RESOURCE_URI = "https://management.azure.com/";
    private static final String DEFAULT_IMDS_BASE_URL = "http://169.254.169.254";
    private static final String DEFAULT_IMDS_API_VERSION = "2018-02-01";

    private static class AzureImdsTokenResponse {
        @SerializedName("access_token")
        String accessToken;
    }

    private final Client conjurClient;
    private final Client imdsClient;
    private final String conjurUri;
    private final String conjurAccount;
    private final String identity;
    private final String authenticator;
    private final String resourceUri;
    private final String clientId;
    private final String imdsBaseUrl;
    private final String imdsApiVersion;

    public AzureAuthenticator(
            String conjurUri,
            String conjurAccount,
            String identity,
            String authenticator) {
        this(
                ClientBuilder.newClient(),
                ClientBuilder.newClient(),
                conjurUri,
                conjurAccount,
                identity,
                authenticator,
                DEFAULT_RESOURCE_URI,
                null,
                DEFAULT_IMDS_BASE_URL,
                DEFAULT_IMDS_API_VERSION
        );
    }

    public AzureAuthenticator(
            String conjurUri,
            String conjurAccount,
            String identity,
            String authenticator,
            String clientId) {
        this(
                ClientBuilder.newClient(),
                ClientBuilder.newClient(),
                conjurUri,
                conjurAccount,
                identity,
                authenticator,
                DEFAULT_RESOURCE_URI,
                clientId,
                DEFAULT_IMDS_BASE_URL,
                DEFAULT_IMDS_API_VERSION
        );
    }

    public AzureAuthenticator(
            Client conjurClient,
            Client imdsClient,
            String conjurUri,
            String conjurAccount,
            String identity,
            String authenticator,
            String resourceUri,
            String clientId,
            String imdsBaseUrl,
            String imdsApiVersion) {
        this.conjurClient = Args.notNull(conjurClient, "conjurClient");
        this.imdsClient = Args.notNull(imdsClient, "imdsClient");
        this.conjurUri = Args.notBlank(conjurUri, "conjurUri");
        this.conjurAccount = Args.notBlank(conjurAccount, "conjurAccount");
        this.identity = Args.notBlank(identity, "identity");
        this.authenticator = requireAuthenticatorWithServiceId(authenticator);
        this.resourceUri = Args.notBlank(resourceUri, "resourceUri");
        this.clientId = clientId;
        this.imdsBaseUrl = Args.notBlank(imdsBaseUrl, "imdsBaseUrl");
        this.imdsApiVersion = Args.notBlank(imdsApiVersion, "imdsApiVersion");
    }

    @Override
    public Token authenticate() {
        String azureJwt = fetchAzureJwt();
        return exchangeJwtForConjurToken(azureJwt);
    }

    @Override
    public Token authenticate(boolean useCachedToken) {
        return authenticate();
    }

    public String getConjurAuthenticateUrl() {
        String normalizedConjurUri = conjurUri.endsWith("/")
                ? conjurUri.substring(0, conjurUri.length() - 1)
                : conjurUri;

        return normalizedConjurUri + "/" + authenticator + "/" + conjurAccount + "/"
                + encodeUriComponent(identity) + "/authenticate";
    }

    String buildImdsTokenUrl() {
        String url = imdsBaseUrl + IMDS_TOKEN_PATH
                + "?api-version=" + encodeQueryParam(imdsApiVersion)
                + "&resource=" + encodeQueryParam(resourceUri);

        if (clientId != null && !clientId.isEmpty()) {
            url += "&client_id=" + encodeQueryParam(clientId);
        }

        return url;
    }

    protected String fetchAzureJwt() {
        String imdsUrl = buildImdsTokenUrl();

        try (Response response = imdsClient
                .target(imdsUrl)
                .request("application/json")
                .header("Metadata", "true")
                .get(Response.class)) {

            validateResponse(response, "Azure IMDS token request failed");
            String body = response.readEntity(String.class);

            AzureImdsTokenResponse tokenResponse =
                    new Gson().fromJson(body, AzureImdsTokenResponse.class);

            if (tokenResponse == null || tokenResponse.accessToken == null || tokenResponse.accessToken.trim().isEmpty()) {
                throw new IllegalStateException("Azure IMDS returned an empty access token.");
            }

            return tokenResponse.accessToken;
        }
    }

    protected Token exchangeJwtForConjurToken(String azureJwt) {
        Form form = new Form().param("jwt", azureJwt);

        try (Response response = conjurClient
                .target(getConjurAuthenticateUrl())
                .request("text/plain")
                .header("Accept-Encoding", "base64")
                .post(Entity.form(form), Response.class)) {

            validateResponse(response, "Conjur Azure authenticate request failed");
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

    // Validates that authenticator includes a service-id segment (e.g. "authn-azure/prod").
    // Without the service-id Conjur returns 404 with no clear indication of the cause.
    private static String requireAuthenticatorWithServiceId(String authenticator) {
        Args.notBlank(authenticator, "authenticator");
        if (!authenticator.contains("/")) {
            throw new IllegalArgumentException(
                    "authenticator must include a service-id (e.g. \"authn-azure/prod\"), got: " + authenticator);
        }
        return authenticator;
    }

    @Override
    public void close() {
        conjurClient.close();
        imdsClient.close();
    }
}