package net.conjur.api;

import net.conjur.api.authn.AuthnProvider;
import net.conjur.api.authn.TokenAuthFilter;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.sql.ClientInfoStatus;

public class Resource {
    private AuthnProvider authn;
    private Endpoints endpoints;
    private Client client;

    public Resource(AuthnProvider authn, Endpoints endpoints){
        this.authn = authn;
        this.endpoints = endpoints;
        client = ClientBuilder.newBuilder().register(new TokenAuthFilter(authn)).build();
    }

    public Resource(Resource relative){
        this(relative.getAuthn(), relative.getEndpoints());
    }

    protected AuthnProvider getAuthn() {
        return authn;
    }

    protected Endpoints getEndpoints() {
        return endpoints;
    }

    protected WebTarget target(URI uri){
        return client.target(uri);
    }

    protected Client client(){
        return client;
    }

    protected Client createClient(){
        return ClientBuilder.newBuilder()
                .register(new TokenAuthFilter(authn))
                .register(JacksonFeature.class)
                .build();
    }
}