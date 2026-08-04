package com.cyberark.conjur.api.clients;

import static com.cyberark.conjur.util.EncodeUriComponent.encodeUriComponent;

import com.cyberark.conjur.api.AuthnProvider;
import com.cyberark.conjur.api.Token;
import com.cyberark.conjur.util.Args;
import com.google.gson.Gson;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AWSIAMAuthenticator implements AuthnProvider, AutoCloseable {

    private static final Pattern VALID_REGION_PATTERN =
            Pattern.compile("^[a-z]{2,3}(-[a-z]+){1,2}-\\d{1,2}$");

    private final Client conjurClient;
    private final String conjurUri;
    private final String conjurAccount;
    private final String identity;
    private final String authenticator;
    private final String awsRegion;

    public AWSIAMAuthenticator(
            String conjurUri,
            String conjurAccount,
            String identity) {
        this(
                ClientBuilder.newClient(),
                conjurUri,
                conjurAccount,
                identity,
                "authn-iam/prod",
                "us-east-1"
        );
    }

    public AWSIAMAuthenticator(
            Client conjurClient,
            String conjurUri,
            String conjurAccount,
            String identity,
            String authenticator,
            String awsRegion) {
        this.conjurClient = Args.notNull(conjurClient, "conjurClient");
        this.conjurUri = Args.notBlank(conjurUri, "conjurUri");
        this.conjurAccount = Args.notBlank(conjurAccount, "conjurAccount");
        this.identity = Args.notBlank(identity, "identity");
        this.authenticator = requireAuthenticatorWithServiceId(authenticator);
        this.awsRegion = Args.notBlank(awsRegion, "awsRegion");
    }

    @Override
    public Token authenticate() {
        String headersJson = buildSignedHeaders();
        return exchangeHeadersForConjurToken(headersJson);
    }

    @Override
    public Token authenticate(boolean useCachedToken) {
        return authenticate();
    }

    String buildSignedHeaders() {
        if (!isValidAwsRegion(awsRegion)) {
            throw new IllegalArgumentException("Invalid AWS region: " + awsRegion);
        }

        String stsEndpoint = "global".equals(awsRegion)
                ? "https://sts.amazonaws.com/?Action=GetCallerIdentity&Version=2011-06-15"
                : "https://sts." + awsRegion + ".amazonaws.com/?Action=GetCallerIdentity&Version=2011-06-15";

        URI uri = URI.create(stsEndpoint);
        String host = uri.getHost();
        if (!isValidAwsHost(host)) {
            throw new IllegalArgumentException("Invalid AWS STS endpoint host: " + host);
        }

        Region region = signingRegionFor(awsRegion);

        SdkHttpFullRequest request = SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.GET)
                .uri(uri)
                .putHeader("Host", host)
                .build();

        Aws4SignerParams signerParams = Aws4SignerParams.builder()
                .awsCredentials(DefaultCredentialsProvider.create().resolveCredentials())
                .signingRegion(region)
                .signingName("sts")
                .build();

        SdkHttpFullRequest signed = Aws4Signer.create().sign(request, signerParams);

        Map<String, String> headers = new LinkedHashMap<>();
        signed.headers().forEach((name, values) -> {
            if (!values.isEmpty()) {
                headers.put(name, values.get(0));
            }
        });

        return new Gson().toJson(headers);
    }

    protected Token exchangeHeadersForConjurToken(String headersJson) {
        try (Response response = conjurClient
                .target(getConjurAuthenticateUrl())
                .request("text/plain")
                .header("Accept-Encoding", "base64")
                .post(Entity.json(headersJson), Response.class)) {

            validateResponse(response, "Conjur AWS IAM authenticate request failed");
            String encodedToken = response.readEntity(String.class);
            return Token.fromJson(base64DecodeToUtf8(encodedToken));
        }
    }

    public String getConjurAuthenticateUrl() {
        String normalizedConjurUri = conjurUri.endsWith("/")
                ? conjurUri.substring(0, conjurUri.length() - 1)
                : conjurUri;
        return normalizedConjurUri + "/" + authenticator + "/" + conjurAccount
                + "/" + encodeUriComponent(identity) + "/authenticate";
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

    static Region signingRegionFor(String awsRegion) {
        return "global".equals(awsRegion) ? Region.US_EAST_1 : Region.of(awsRegion);
    }

    static boolean isValidAwsRegion(String region) {
        if ("global".equals(region)) {
            return true;
        }
        return VALID_REGION_PATTERN.matcher(region).matches();
    }

    static boolean isValidAwsHost(String host) {
        return host != null && host.endsWith(".amazonaws.com");
    }

    // Validates that authenticator includes a service-id segment (e.g. "authn-iam/prod").
    // Without the service-id Conjur returns 404 with no clear indication of the cause.
    private static String requireAuthenticatorWithServiceId(String authenticator) {
        Args.notBlank(authenticator, "authenticator");
        if (!authenticator.contains("/")) {
            throw new IllegalArgumentException(
                    "authenticator must include a service-id (e.g. \"authn-iam/prod\"), got: " + authenticator);
        }
        return authenticator;
    }

    @Override
    public void close() {
        conjurClient.close();
    }
}
