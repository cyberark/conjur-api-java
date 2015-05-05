package net.conjur.util.rs;

import org.apache.commons.codec.binary.Base64;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Adds basic auth to a request.
 */
public class HttpBasicAuthFilter implements ClientRequestFilter {
    /* This is based on the jersey implementation of this class...
     * Worth another look at some point.
     */
    private static final Charset CHARACTER_SET = Charset.forName("iso-8859-1");

    private final String authorizationHeader;

    public HttpBasicAuthFilter(String username, String password){
        String raw = username + ":" + password;
        authorizationHeader = "Basic " +
                Base64.encodeBase64String(raw.getBytes(CHARACTER_SET));
    }

    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        final MultivaluedMap<String, Object> headers = clientRequestContext.getHeaders();
        // Multiple Authorization headers result in undefined behavior, and deleting existing
        // ones seems to violate the principle of least surprise.
        if(!headers.containsKey("Authorization")){
            headers.add("Authorization", authorizationHeader);
        }
    }
}
