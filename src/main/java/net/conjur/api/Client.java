package net.conjur.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import gumi.builders.UrlBuilder;
import net.conjur.api.exceptions.ConjurApiException;
import net.conjur.api.exceptions.http.HttpException;
import net.conjur.util.Args;
import net.conjur.util.Callable;
import net.conjur.util.TextUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Abstract base class for Conjur service clients.
 */
public abstract class Client {

    private final Endpoints endpoints;

    /**
     * Create a service from an endpoint configuration
     *
     * @param endpoints
     */
    public Client(Endpoints endpoints) {
        this.endpoints = Args.notNull(endpoints, "endpoints");
    }


    /**
     * @return The base URI for the service this client talks to
     */
    public abstract URI getUri();

    public URI getUri(String path) {
        if (path == null)
            return getUri();
        if(path.length() > 0){
            if(!path.startsWith("/"))
                path = "/" + path;
            if(path.endsWith("/") && path.length() > 1)
                path = path.substring(0, path.length() - 1);
        }
        return UrlBuilder.fromUri(getUri()).withPath(path).toUri();
    }

    public URI getUri(String... pathParts) {
        return getUri(TextUtils.join("/", pathParts));
    }

    public Endpoints getEndpoints() {
        return endpoints;
    }

    public Response execute(Request request) throws IOException {
        return prepareRequest(request).execute();
    }

    public Response response(final Request request) {
        return wrapHttpExceptions(new Callable<Response>() {
            public Response call() throws Exception {
                return execute(request);
            }
        });
    }

    public String responseString(final Request request){
        return wrapHttpExceptions(new Callable<String>() {
            public String call() throws Exception {
                return execute(request).returnContent().asString();
            }
        });
    }

    public JsonElement responseJson(final Request request){
        return new JsonParser().parse(responseString(request));
    }

    public <T> T responseJson(final Request request, Class<T> asType){
        return new Gson().fromJson(responseJson(request), asType);
    }

    public InputStream responseStream(final Request request){
        return wrapHttpExceptions(new Callable<InputStream>() {
            public InputStream call() throws Exception {
                return execute(request).returnContent().asStream();
            }
        });
    }

    protected Request prepareRequest(Request request) {
        return request;
    }

    protected <T> T wrapHttpExceptions(Callable<T> callable) {
        try {
            return callable.call();
        } catch (HttpResponseException e) {
            throw HttpException.fromHttpResponseException(e);
        } catch (IOException e) {
            throw new ConjurApiException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
