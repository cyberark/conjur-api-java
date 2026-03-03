package com.cyberark.conjur.api.clients;

import javax.net.ssl.SSLContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.cyberark.conjur.api.ConjurResource;
import com.cyberark.conjur.api.Configuration;
import com.cyberark.conjur.api.Credentials;
import com.cyberark.conjur.api.Endpoints;
import com.cyberark.conjur.api.ResourceProvider;
import com.cyberark.conjur.api.Token;
import com.cyberark.conjur.util.EncodeUriComponent;
import com.cyberark.conjur.util.rs.TokenAuthFilter;

/**
 * Conjur service client.
 */
public class ResourceClient implements ResourceProvider {

    private static final Type MAP_STRING_STRING_TYPE =
            new TypeToken<Map<String, String>>(){}.getType();
    private static final Type LIST_RESOURCE_TYPE =
            new TypeToken<List<ConjurResource>>(){}.getType();
    private static final Gson GSON = new Gson();

    private Client client;
    private WebTarget secrets;
    private final Endpoints endpoints;

    public ResourceClient(final Credentials credentials, final Endpoints endpoints) {
        this(credentials, endpoints, null);
    }

    public ResourceClient(final Credentials credentials,
                          final Endpoints endpoints,
                          final SSLContext sslContext) {
        this.endpoints = endpoints;

        init(credentials, sslContext);
    }

    // Build ResourceClient using a Conjur auth token
    public ResourceClient(final Token token, final Endpoints endpoints) {
        this(token, endpoints, null);
    }

    // Build ResourceClient using a Conjur auth token
    public ResourceClient(final Token token,
                          final Endpoints endpoints,
                          final SSLContext sslContext) {
        this.endpoints = endpoints;

        init(token, sslContext);
    }

    // Package-private constructor for unit testing with mock clients
    ResourceClient(Client client, WebTarget secrets, Endpoints endpoints) {
        this.client = client;
        this.secrets = secrets;
        this.endpoints = endpoints;
    }

    @Override
    public String retrieveSecret(String variableId) {
        Response response = secrets.path(encodeVariableId(variableId))
          .request().get(Response.class);
        validateResponse(response);

        return response.readEntity(String.class);
    }

    @Override
    public void addSecret(String variableId, String secret) {
        Response response = secrets.path(encodeVariableId(variableId)).request()
          .post(Entity.text(secret), Response.class);
        validateResponse(response);
    }

    /**
     * Fetch multiple secret values in one invocation using the batch retrieval API.
     * <p>
     * Constructs fully-qualified variable IDs ({account}:variable:{id}) and sends them
     * as a comma-delimited list in the {@code variable_ids} query parameter.
     * </p>
     *
     * @param variableIds the variable IDs to retrieve (without account/kind prefix)
     * @return a map of variable ID (as passed by caller) to secret value
     * @throws IllegalArgumentException if no variable IDs are provided or account is not configured
     * @throws WebApplicationException if the server returns an error response
     * @see <a href="https://docs.cyberark.com/conjur-open-source/latest/en/content/developer/conjur_api_batch_retrieve.htm">Batch Secret Retrieval</a>
     */
    @Override
    public Map<String, String> retrieveBatchSecrets(String... variableIds) {
        if (variableIds == null || variableIds.length == 0) {
            throw new IllegalArgumentException("At least one variable ID must be provided");
        }

        String account = endpoints.getAccount();
        if (account == null || account.isEmpty()) {
            throw new IllegalArgumentException("Account is not configured in Endpoints");
        }

        // Build the comma-delimited fully-qualified variable IDs for the query parameter.
        // Format: {account}:variable:{encoded_id1},{account}:variable:{encoded_id2}
        // Colons and commas are valid in URI query components (RFC 3986) and must NOT be encoded.
        // Only the variable ID portion is percent-encoded.
        String queryValue = buildBatchQueryParam(account, variableIds);

        // Build the full URI manually to avoid double-encoding by JAX-RS queryParam()
        URI batchUri = URI.create(endpoints.getBatchSecretsUri().toString()
                + "?variable_ids=" + queryValue);

        Response response = client.target(batchUri).request().get(Response.class);
        validateResponse(response);

        String json = response.readEntity(String.class);
        Map<String, String> raw = GSON.fromJson(json, MAP_STRING_STRING_TYPE);

        // Map fully-qualified IDs back to the caller's variable IDs
        String prefix = account + ":variable:";
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : raw.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                result.put(key.substring(prefix.length()), entry.getValue());
            } else {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }

    /**
     * List all resources visible to the authenticated identity.
     *
     * @return list of all resources
     * @see <a href="https://docs.cyberark.com/conjur-open-source/latest/en/content/developer/conjur_api_list_resources.htm">List Resources</a>
     */
    @Override
    public List<ConjurResource> listResources() {
        return listResources(null, null, null, null);
    }

    /**
     * List resources filtered by kind.
     *
     * @param kind the resource kind (e.g. "variable", "host", "user", "group", "layer", "policy", "webservice")
     * @return resources matching the given kind
     */
    @Override
    public List<ConjurResource> listResources(String kind) {
        return listResources(kind, null, null, null);
    }

    /**
     * List resources with full query parameter control.
     *
     * @param kind   resource kind filter (null for all kinds)
     * @param search text search filter (null for no search)
     * @param limit  max results per page (null for server default, max 1000)
     * @param offset pagination offset (null for no offset)
     * @return resources matching the query
     * @throws WebApplicationException if the server returns an error response
     */
    @Override
    public List<ConjurResource> listResources(String kind, String search, Integer limit, Integer offset) {
        URI resourcesUri = endpoints.getResourcesUri();
        StringBuilder uriBuilder = new StringBuilder(resourcesUri.toString());
        String separator = "?";

        if (kind != null && !kind.isEmpty()) {
            uriBuilder.append(separator).append("kind=").append(encodeVariableId(kind));
            separator = "&";
        }
        if (search != null && !search.isEmpty()) {
            uriBuilder.append(separator).append("search=").append(encodeVariableId(search));
            separator = "&";
        }
        if (limit != null) {
            uriBuilder.append(separator).append("limit=").append(limit);
            separator = "&";
        }
        if (offset != null) {
            uriBuilder.append(separator).append("offset=").append(offset);
        }

        URI targetUri = URI.create(uriBuilder.toString());
        Response response = client.target(targetUri).request().get(Response.class);
        validateResponse(response);

        String json = response.readEntity(String.class);
        List<ConjurResource> resources = GSON.fromJson(json, LIST_RESOURCE_TYPE);
        return resources != null ? resources : Collections.<ConjurResource>emptyList();
    }

    /**
     * Count resources visible to the authenticated identity.
     *
     * @param kind   resource kind filter (null for all kinds)
     * @param search text search filter (null for no search)
     * @return the number of matching resources
     * @throws WebApplicationException if the server returns an error response
     */
    @Override
    public int countResources(String kind, String search) {
        URI resourcesUri = endpoints.getResourcesUri();
        StringBuilder uriBuilder = new StringBuilder(resourcesUri.toString());
        uriBuilder.append("?count=true");

        if (kind != null && !kind.isEmpty()) {
            uriBuilder.append("&kind=").append(encodeVariableId(kind));
        }
        if (search != null && !search.isEmpty()) {
            uriBuilder.append("&search=").append(encodeVariableId(search));
        }

        URI targetUri = URI.create(uriBuilder.toString());
        Response response = client.target(targetUri).request().get(Response.class);
        validateResponse(response);

        String body = response.readEntity(String.class).trim();

        // The server may return a plain integer or a JSON object like {"count":N}
        if (body.startsWith("{")) {
            @SuppressWarnings("unchecked")
            Map<String, Double> parsed = GSON.fromJson(body, Map.class);
            Double count = parsed.get("count");
            if (count == null) {
                throw new IllegalStateException("Unexpected count response: " + body);
            }
            return count.intValue();
        }
        return Integer.parseInt(body);
    }

    /**
     * Build the comma-separated query parameter value for batch retrieval.
     * Visible for testing.
     */
    String buildBatchQueryParam(String account, String... variableIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < variableIds.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(account).append(":variable:")
              .append(encodeVariableId(variableIds[i]));
        }
        return sb.toString();
    }

    // The "encodeUriComponent" method encodes plus signs into %2B and spaces
    // into '+'. However, our server decodes plus signs into plus signs in the
    // retrieveSecret request so we need to replace the plus signs (which are
    // spaces) into %20.
    private String encodeVariableId(String variableId) {
        return EncodeUriComponent.encodeUriComponent(variableId).replaceAll("\\+", "%20");
    }

    private Endpoints getEndpoints() {
        return endpoints;
    }

    private void init(Credentials credentials, SSLContext sslContext){
        Configuration config = new Configuration();

        ClientBuilder builder = ClientBuilder.newBuilder()
            .register(new TokenAuthFilter(new AuthnClient(credentials, endpoints, sslContext)))
            .register(new TelemetryHeaderFilter(config)); // Register TelemetryHeaderFilter
                
        if(sslContext != null) {
            builder.sslContext(sslContext);
        }

        this.client = builder.build();

        secrets = client.target(getEndpoints().getSecretsUri());
    }

    private void init(Token token, SSLContext sslContext){
        Configuration config = new Configuration();

        ClientBuilder builder = ClientBuilder.newBuilder()
                .register(new TokenAuthFilter(new AuthnTokenClient(token)))
                .register(new TelemetryHeaderFilter(config)); // Register TelemetryHeaderFilter

        if(sslContext != null) {
            builder.sslContext(sslContext);
        }

        this.client = builder.build();

        secrets = client.target(getEndpoints().getSecretsUri());
    }

    // TODO orenbm: Remove when we have a response filter to handle this
    private void validateResponse(Response response) {
        int status = response.getStatus();
        if (status < 200 || status >= 400) {
            String errorMessage = String.format("Error code: %d, Error message: %s", status, response.readEntity(String.class));
            throw new WebApplicationException(errorMessage, status);
        }
    }
}
