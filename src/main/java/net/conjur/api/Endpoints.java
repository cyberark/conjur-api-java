package net.conjur.api;

import net.conjur.util.Args;
import java.io.Serializable;
import java.net.URI;

/**
 * An <code>Endpoints</code> instance provides endpoint URIs for the various conjur services.
 */
public class Endpoints implements Serializable {
    private static final String URL_PROPERTY_NAME = "CONJUR_APPLIANCE_URL";
    private static final String ACCOUNT_PROPERTY_NAME = "CONJUR_ACCOUNT";

    private final URI authnUri;
    private final URI secretsUri;

    public Endpoints(final URI authnUri, final URI secretsUri){
        this.authnUri = Args.notNull(authnUri, "authnUri");
        this.secretsUri = Args.notNull(secretsUri, "secretsUri");
    }

    public Endpoints(String authnUri, String secretsUri){
        this(URI.create(authnUri), URI.create(secretsUri));
    }

    public URI getAuthnUri(){ return authnUri; }

    public URI getSecretsUri() {
        return secretsUri;
    }

    public static Endpoints fromSystemProperties(){

        String account = System.getProperty(ACCOUNT_PROPERTY_NAME);

        return new Endpoints(
                getServiceUri("authn", account),
                getServiceUri("secrets", account, "variable")
        );
    }

    private static URI getServiceUri(String service, String accountName){
        return getServiceUri(service, accountName, "");
    }

    private static URI getServiceUri(String service, String accountName, String path){
        return URI.create(String.format("%s/%s/%s/%s", System.getProperty(URL_PROPERTY_NAME), service, accountName, path));
    }

    @Override
    public String toString() {
        return "Endpoints{" +
                "authnUri=" + authnUri +
                "secretsUri=" + secretsUri +
                '}';
    }
}
